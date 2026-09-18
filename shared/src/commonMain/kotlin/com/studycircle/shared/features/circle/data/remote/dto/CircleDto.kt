package com.studycircle.shared.features.circle.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCircleRequestDto(val name: String)

@Serializable
data class JoinCircleRequestDto(val joinCode: String)

// createdAt travels as an ISO-8601 string (that's how the backend's
// java.time.Instant serializes by default) — parsed to kotlinx.datetime.Instant
// in the repository, not here.
@Serializable
data class CircleResponseDto(
    val id: String,
    val name: String,
    val joinCode: String,
    val supervisorId: String,
    val memberIds: List<String>,
    val createdAt: String,
    val isActive: Boolean
)

@Serializable
data class CircleMemberSummaryDto(val id: String, val displayName: String, val role: String)

@Serializable
data class CircleDetailResponseDto(
    val id: String,
    val name: String,
    val joinCode: String,
    val supervisorId: String,
    val members: List<CircleMemberSummaryDto>,
    val createdAt: String,
    val isActive: Boolean
)
