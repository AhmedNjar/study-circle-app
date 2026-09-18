package com.studycircle.backend.submission

import com.fasterxml.jackson.databind.ObjectMapper
import com.studycircle.backend.common.EvidenceTypeMismatchException
import com.studycircle.backend.common.NotGroupMemberException
import com.studycircle.backend.common.SubmissionBelowThresholdException
import com.studycircle.backend.common.SubmissionNotFoundException
import com.studycircle.backend.common.SubmitterMismatchException
import com.studycircle.backend.common.TaskGroupNotFoundException
import com.studycircle.backend.streak.StreakService
import com.studycircle.backend.submission.dto.ReviewSubmissionRequest
import com.studycircle.backend.submission.dto.SubmissionEvidenceDto
import com.studycircle.backend.submission.dto.SubmissionResponse
import com.studycircle.backend.submission.dto.SubmitSubmissionRequest
import com.studycircle.backend.submission.dto.SubmitterRefDto
import com.studycircle.backend.task.GroupCompletionRule
import com.studycircle.backend.task.TaskGroupService
import com.studycircle.backend.task.TaskService
import com.studycircle.backend.task.dto.TrackingMethodDto
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val taskService: TaskService,
    private val taskGroupService: TaskGroupService,
    private val streakService: StreakService,
    private val objectMapper: ObjectMapper
) {
    fun submit(taskId: String, request: SubmitSubmissionRequest, requesterId: String): SubmissionResponse {
        val submitter = request.submitter
        val actingAs = when (submitter) {
            is SubmitterRefDto.Individual -> submitter.userId
            is SubmitterRefDto.OnBehalfOfGroup -> submitter.submittedByUserId
        }
        if (actingAs != requesterId) throw SubmitterMismatchException()

        if (submitter is SubmitterRefDto.OnBehalfOfGroup) {
            val group = taskGroupService.getEntity(submitter.groupId)
            if (group.taskId != taskId) throw TaskGroupNotFoundException()
            if (requesterId !in taskGroupService.memberIdsOf(group.id)) throw NotGroupMemberException()
        }

        val task = taskService.getEntity(taskId)
        val tracking = taskService.trackingOf(task)
        validate(tracking, request.evidence)

        val entity = SubmissionEntity(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            submitterJson = objectMapper.writeValueAsString(request.submitter),
            submittedAt = Instant.now(),
            status = SubmissionStatus.PENDING,
            evidenceJson = objectMapper.writeValueAsString(request.evidence)
        )
        submissionRepository.save(entity)
        return entity.toResponse()
    }

    fun listByTask(taskId: String, status: String?): List<SubmissionResponse> {
        val entities = if (status != null) {
            submissionRepository.findByTaskIdAndStatus(taskId, SubmissionStatus.valueOf(status))
        } else {
            submissionRepository.findByTaskId(taskId)
        }
        return entities.map { it.toResponse() }
    }

    fun review(submissionId: String, request: ReviewSubmissionRequest): SubmissionResponse {
        val entity = submissionRepository.findById(submissionId).orElseThrow { SubmissionNotFoundException() }
        val newStatus = SubmissionStatus.valueOf(request.status)
        entity.status = newStatus
        entity.supervisorNote = request.supervisorNote
        submissionRepository.save(entity)

        if (newStatus == SubmissionStatus.APPROVED) {
            recordStreakCompletion(entity)
        }
        return entity.toResponse()
    }

    private fun recordStreakCompletion(entity: SubmissionEntity) {
        val submitter = objectMapper.readValue(entity.submitterJson, SubmitterRefDto::class.java)
        val task = taskService.getEntity(entity.taskId)
        val completionDate = entity.submittedAt.atZone(ZoneOffset.UTC).toLocalDate()

        when (submitter) {
            is SubmitterRefDto.Individual ->
                streakService.recordCompletion(task.circleId, submitter.userId, completionDate)
            is SubmitterRefDto.OnBehalfOfGroup ->
                recordGroupStreakIfComplete(task.circleId, submitter.groupId, entity.taskId, completionDate)
        }
    }

    // A group task's members each submit their OWN evidence, tagged with the
    // group they're doing it as part of — this isn't "one person submits for
    // everyone". completionRule decides when the GROUP's collective الرِّباط
    // advances: once every member has an approved submission (ALL_MUST_SUBMIT)
    // or as soon as any one does (ANY_SUFFICES). Called on every approval
    // within the group — safe to call repeatedly, since StreakMath's own
    // day-math already no-ops a same-day re-completion.
    private fun recordGroupStreakIfComplete(circleId: String, groupId: String, taskId: String, completionDate: LocalDate) {
        val group = taskGroupService.getEntity(groupId)
        val memberIds = taskGroupService.memberIdsOf(groupId).toSet()
        if (memberIds.isEmpty()) return

        val approvedMemberIds = submissionRepository.findByTaskId(taskId)
            .filter { it.status == SubmissionStatus.APPROVED }
            .mapNotNull { sub ->
                (objectMapper.readValue(sub.submitterJson, SubmitterRefDto::class.java) as? SubmitterRefDto.OnBehalfOfGroup)
                    ?.takeIf { it.groupId == groupId }
                    ?.submittedByUserId
            }
            .toSet()

        val isComplete = when (group.completionRule) {
            GroupCompletionRule.ALL_MUST_SUBMIT -> approvedMemberIds.containsAll(memberIds)
            GroupCompletionRule.ANY_SUFFICES -> approvedMemberIds.isNotEmpty()
        }
        if (isComplete) {
            streakService.recordGroupCompletion(circleId, groupId, completionDate)
        }
    }

    // The anti-idle-cheating guard from the original spec, enforced
    // server-side (never trust the client) even though the shared KMP module
    // runs the identical check first for instant client-side feedback.
    private fun validate(tracking: TrackingMethodDto, evidence: SubmissionEvidenceDto) {
        when (tracking) {
            is TrackingMethodDto.TimedWithReflection -> {
                val ev = evidence as? SubmissionEvidenceDto.TimedReflection ?: throw EvidenceTypeMismatchException()
                if (ev.actualDurationSeconds < tracking.minDurationSeconds ||
                    ev.reflectionText.length < tracking.reflectionMinLength
                ) {
                    throw SubmissionBelowThresholdException()
                }
            }
            is TrackingMethodDto.MediaSubmission -> {
                val ev = evidence as? SubmissionEvidenceDto.Media ?: throw EvidenceTypeMismatchException()
                val minDuration = tracking.minDurationSeconds
                if (minDuration != null && ev.durationSeconds < minDuration) {
                    throw SubmissionBelowThresholdException()
                }
            }
        }
    }

    private fun SubmissionEntity.toResponse() = SubmissionResponse(
        id = id, taskId = taskId,
        submitter = objectMapper.readValue(submitterJson, SubmitterRefDto::class.java),
        submittedAt = submittedAt.toString(), status = status.name, supervisorNote = supervisorNote,
        evidence = objectMapper.readValue(evidenceJson, SubmissionEvidenceDto::class.java)
    )
}
