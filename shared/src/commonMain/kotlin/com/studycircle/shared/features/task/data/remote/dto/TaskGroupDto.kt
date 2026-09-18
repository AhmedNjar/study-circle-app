package com.studycircle.shared.features.task.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FormOrJoinGroupRequestDto(val groupId: String? = null, val name: String? = null)

@Serializable
data class TaskGroupResponseDto(
    val id: String,
    val taskId: String,
    val name: String? = null,
    val memberIds: List<String>,
    val completionRule: String
)
