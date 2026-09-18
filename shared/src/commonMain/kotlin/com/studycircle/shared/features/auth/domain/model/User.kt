package com.studycircle.shared.features.auth.domain.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val role: Role,
    val createdAt: Instant
)
