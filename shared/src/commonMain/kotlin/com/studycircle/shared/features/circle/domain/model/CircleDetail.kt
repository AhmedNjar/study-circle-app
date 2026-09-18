package com.studycircle.shared.features.circle.domain.model

import com.studycircle.shared.features.auth.domain.model.Role
import kotlinx.datetime.Instant

// Richer than Circle (which only carries memberIds) — this is what the
// circle detail screen renders: names and roles, not just ids.
data class CircleMemberSummary(val id: String, val displayName: String, val role: Role)

data class CircleDetail(
    val id: String,
    val name: String,
    val joinCode: String,
    val supervisorId: String,
    val members: List<CircleMemberSummary>,
    val createdAt: Instant,
    val isActive: Boolean
)
