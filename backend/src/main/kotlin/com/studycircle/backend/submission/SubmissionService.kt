package com.studycircle.backend.submission

import com.fasterxml.jackson.databind.ObjectMapper
import com.studycircle.backend.common.EvidenceTypeMismatchException
import com.studycircle.backend.common.SubmissionBelowThresholdException
import com.studycircle.backend.common.SubmissionNotFoundException
import com.studycircle.backend.common.SubmitterMismatchException
import com.studycircle.backend.common.UnsupportedSubmitterException
import com.studycircle.backend.submission.dto.ReviewSubmissionRequest
import com.studycircle.backend.submission.dto.SubmissionEvidenceDto
import com.studycircle.backend.submission.dto.SubmissionResponse
import com.studycircle.backend.submission.dto.SubmitSubmissionRequest
import com.studycircle.backend.submission.dto.SubmitterRefDto
import com.studycircle.backend.task.TaskService
import com.studycircle.backend.task.dto.TrackingMethodDto
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val taskService: TaskService,
    private val objectMapper: ObjectMapper
) {
    fun submit(taskId: String, request: SubmitSubmissionRequest, requesterId: String): SubmissionResponse {
        val submitter = request.submitter as? SubmitterRefDto.Individual ?: throw UnsupportedSubmitterException()
        if (submitter.userId != requesterId) throw SubmitterMismatchException()

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
        entity.status = SubmissionStatus.valueOf(request.status)
        entity.supervisorNote = request.supervisorNote
        submissionRepository.save(entity)
        // TODO Phase 3: an APPROVED transition here is exactly where
        // CalculateStreakUseCase fires — see the implementation plan.
        return entity.toResponse()
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
