package com.studycircle.backend.task

import org.springframework.data.jpa.repository.JpaRepository

interface TaskGroupRepository : JpaRepository<TaskGroupEntity, String> {
    fun findByTaskId(taskId: String): List<TaskGroupEntity>
}

interface TaskGroupMemberRepository : JpaRepository<TaskGroupMemberEntity, TaskGroupMemberId> {
    fun findByTaskGroupId(taskGroupId: String): List<TaskGroupMemberEntity>
    fun existsByTaskGroupIdAndUserId(taskGroupId: String, userId: String): Boolean
}
