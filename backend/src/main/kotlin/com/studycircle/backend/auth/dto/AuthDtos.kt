package com.studycircle.backend.auth.dto

import com.studycircle.backend.user.Role

data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val role: Role
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserSummary(
    val id: String,
    val username: String,
    val displayName: String,
    val role: Role
)

data class AuthResponse(
    val token: String,
    val user: UserSummary
)
