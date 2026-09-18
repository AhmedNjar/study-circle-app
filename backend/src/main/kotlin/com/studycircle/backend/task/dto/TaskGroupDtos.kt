package com.studycircle.backend.task.dto

data class FormOrJoinGroupRequest(val groupId: String? = null, val name: String? = null)

data class TaskGroupResponse(
    val id: String,
    val taskId: String,
    val name: String?,
    val memberIds: List<String>,
    val completionRule: String
)
