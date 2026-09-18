package com.studycircle.shared.features.auth.domain.model

// Deliberately its own model, not the User entity — a login/register response
// only ever carries id/username/displayName/role, never createdAt, so
// reusing User here would mean a fake/nullable field for no reason.
data class Session(
    val token: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val role: Role
)
