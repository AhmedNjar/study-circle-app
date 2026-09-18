package com.studycircle.shared.features.circle.domain.model

import kotlinx.datetime.Instant

data class Circle(
    val id: String,
    val name: String,
    val joinCode: String,
    val supervisorId: String,
    val memberIds: List<String>,
    val createdAt: Instant,
    val isActive: Boolean
)
