package com.studycircle.backend.circle.dto

import com.studycircle.backend.user.Role
import java.time.Instant

data class CreateCircleRequest(val name: String)
data class JoinCircleRequest(val joinCode: String)

data class CircleResponse(
    val id: String,
    val name: String,
    val joinCode: String,
    val supervisorId: String,
    val memberIds: List<String>,
    val createdAt: Instant,
    val isActive: Boolean
)

data class MemberSummary(val id: String, val displayName: String, val role: Role)

data class CircleDetailResponse(
    val id: String,
    val name: String,
    val joinCode: String,
    val supervisorId: String,
    val members: List<MemberSummary>,
    val createdAt: Instant,
    val isActive: Boolean
)
