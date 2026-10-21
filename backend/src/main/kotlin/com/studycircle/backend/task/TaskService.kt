package com.studycircle.backend.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.studycircle.backend.circle.CircleMemberRepository
import com.studycircle.backend.circle.CircleRepository
import com.studycircle.backend.common.CircleNotFoundException
import com.studycircle.backend.common.InvalidTrackingException
import com.studycircle.backend.common.NotCircleMemberException
import com.studycircle.backend.common.NotCircleSupervisorException
import com.studycircle.backend.common.TaskNotFoundException
import com.studycircle.backend.task.dto.CreateTaskRequest
import com.studycircle.backend.task.dto.TaskAudienceDto
import com.studycircle.backend.task.dto.TaskMetadataDto
import com.studycircle.backend.task.dto.TaskResponse
import com.studycircle.backend.task.dto.TrackingMethodDto
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val circleRepository: CircleRepository,
    private val memberRepository: CircleMemberRepository,
    private val taskGroupRepository: TaskGroupRepository,
    private val objectMapper: ObjectMapper
) {
    fun create(circleId: String, request: CreateTaskRequest, supervisorId: String): TaskResponse {
        val circle = circleRepository.findById(circleId).orElseThrow { CircleNotFoundException() }
        if (circle.supervisorId != supervisorId) throw NotCircleSupervisorException()
        (request.tracking as? TrackingMethodDto.CountBased)?.let {
            if (it.target <= 0 || it.unit.isBlank()) throw InvalidTrackingException()
        }

        val entity = TaskEntity(
            id = UUID.randomUUID().toString(),
            circleId = circleId,
            title = request.title,
            dueDate = LocalDate.parse(request.dueDate),
            createdBy = supervisorId,
            activityType = request.activityType,
            trackingJson = objectMapper.writeValueAsString(request.tracking),
            audienceJson = objectMapper.writeValueAsString(request.audience),
            metadataJson = objectMapper.writeValueAsString(request.metadata)
        )
        taskRepository.save(entity)
        return entity.toResponse()
    }

    fun listByCircle(circleId: String, requesterId: String): List<TaskResponse> {
        val circle = circleRepository.findById(circleId).orElseThrow { CircleNotFoundException() }
        val isMember = memberRepository.existsByCircleIdAndUserId(circleId, requesterId)
        if (circle.supervisorId != requesterId && !isMember) throw NotCircleMemberException()

        return taskRepository.findByCircleId(circleId).map { it.toResponse() }
    }

    // Authorization helpers for SubmissionService. A task id alone is not proof of
    // access: without these, any authenticated supervisor could list or review
    // another circle's submissions, and any student could submit to a circle they
    // never joined.
    internal fun requireSupervisorOf(taskId: String, userId: String): TaskEntity {
        val task = getEntity(taskId)
        val circle = circleRepository.findById(task.circleId).orElseThrow { CircleNotFoundException() }
        if (circle.supervisorId != userId) throw NotCircleSupervisorException()
        return task
    }

    internal fun requireMemberOf(taskId: String, userId: String): TaskEntity {
        val task = getEntity(taskId)
        if (!memberRepository.existsByCircleIdAndUserId(task.circleId, userId)) throw NotCircleMemberException()
        return task
    }

    // Package-internal so SubmissionService can look up a task's tracking
    // method to validate evidence against, without re-deserializing itself.
    internal fun getEntity(taskId: String): TaskEntity =
        taskRepository.findById(taskId).orElseThrow { TaskNotFoundException() }

    internal fun trackingOf(task: TaskEntity): TrackingMethodDto =
        objectMapper.readValue(task.trackingJson, TrackingMethodDto::class.java)

    internal fun audienceOf(task: TaskEntity): TaskAudienceDto =
        objectMapper.readValue(task.audienceJson, TaskAudienceDto::class.java)

    private fun TaskEntity.toResponse() = TaskResponse(
        id = id, circleId = circleId, title = title, dueDate = dueDate.toString(),
        createdBy = createdBy, activityType = activityType,
        tracking = objectMapper.readValue(trackingJson, TrackingMethodDto::class.java),
        // GROUPS audience is resolved live from TaskGroupRepository rather than
        // trusting whatever groupIds happened to be stored at creation time —
        // groups form well after the task does, so a stored list would just
        // go stale the moment the first group forms.
        audience = audienceOf(this).let { audience ->
            if (audience is TaskAudienceDto.Groups) {
                TaskAudienceDto.Groups(groupIds = taskGroupRepository.findByTaskId(id).map { it.id })
            } else {
                audience
            }
        },
        metadata = objectMapper.readValue(metadataJson, TaskMetadataDto::class.java)
    )
}
