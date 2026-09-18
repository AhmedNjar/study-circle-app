package com.studycircle.backend.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.studycircle.backend.circle.CircleMemberRepository
import com.studycircle.backend.circle.CircleRepository
import com.studycircle.backend.common.CircleNotFoundException
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
    private val objectMapper: ObjectMapper
) {
    fun create(circleId: String, request: CreateTaskRequest, supervisorId: String): TaskResponse {
        val circle = circleRepository.findById(circleId).orElseThrow { CircleNotFoundException() }
        if (circle.supervisorId != supervisorId) throw NotCircleSupervisorException()

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

    // Package-internal so SubmissionService can look up a task's tracking
    // method to validate evidence against, without re-deserializing itself.
    internal fun getEntity(taskId: String): TaskEntity =
        taskRepository.findById(taskId).orElseThrow { TaskNotFoundException() }

    internal fun trackingOf(task: TaskEntity): TrackingMethodDto =
        objectMapper.readValue(task.trackingJson, TrackingMethodDto::class.java)

    private fun TaskEntity.toResponse() = TaskResponse(
        id = id, circleId = circleId, title = title, dueDate = dueDate.toString(),
        createdBy = createdBy, activityType = activityType,
        tracking = objectMapper.readValue(trackingJson, TrackingMethodDto::class.java),
        audience = objectMapper.readValue(audienceJson, TaskAudienceDto::class.java),
        metadata = objectMapper.readValue(metadataJson, TaskMetadataDto::class.java)
    )
}
