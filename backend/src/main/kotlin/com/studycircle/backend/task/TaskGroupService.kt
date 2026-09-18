package com.studycircle.backend.task

import com.studycircle.backend.common.AlreadyInTaskGroupException
import com.studycircle.backend.common.TaskGroupNotFoundException
import com.studycircle.backend.common.TaskNotGroupTaskException
import com.studycircle.backend.task.dto.FormOrJoinGroupRequest
import com.studycircle.backend.task.dto.TaskAudienceDto
import com.studycircle.backend.task.dto.TaskGroupResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TaskGroupService(
    private val taskGroupRepository: TaskGroupRepository,
    private val memberRepository: TaskGroupMemberRepository,
    private val taskService: TaskService
) {
    fun formOrJoin(taskId: String, request: FormOrJoinGroupRequest, userId: String): TaskGroupResponse {
        val task = taskService.getEntity(taskId)
        if (taskService.audienceOf(task) !is TaskAudienceDto.Groups) throw TaskNotGroupTaskException()

        val alreadyInAGroup = taskGroupRepository.findByTaskId(taskId)
            .any { memberRepository.existsByTaskGroupIdAndUserId(it.id, userId) }

        val group = if (request.groupId != null) {
            val existing = taskGroupRepository.findById(request.groupId).orElseThrow { TaskGroupNotFoundException() }
            if (existing.taskId != taskId) throw TaskGroupNotFoundException()
            if (!alreadyInAGroup) memberRepository.save(TaskGroupMemberEntity(existing.id, userId))
            existing
        } else {
            if (alreadyInAGroup) throw AlreadyInTaskGroupException()
            // completionRule defaults to ALL_MUST_SUBMIT for every group —
            // Phase 4 doesn't yet let a supervisor pick a per-task default;
            // see GETTING_STARTED.md.
            val created = TaskGroupEntity(
                id = UUID.randomUUID().toString(), taskId = taskId,
                name = request.name, completionRule = GroupCompletionRule.ALL_MUST_SUBMIT
            )
            taskGroupRepository.save(created)
            memberRepository.save(TaskGroupMemberEntity(created.id, userId))
            created
        }
        return group.toResponse()
    }

    fun listForTask(taskId: String): List<TaskGroupResponse> =
        taskGroupRepository.findByTaskId(taskId).map { it.toResponse() }

    internal fun getEntity(groupId: String): TaskGroupEntity =
        taskGroupRepository.findById(groupId).orElseThrow { TaskGroupNotFoundException() }

    internal fun memberIdsOf(groupId: String): List<String> =
        memberRepository.findByTaskGroupId(groupId).map { it.userId }

    private fun TaskGroupEntity.toResponse() = TaskGroupResponse(
        id = id, taskId = taskId, name = name,
        memberIds = memberIdsOf(id), completionRule = completionRule.name
    )
}
