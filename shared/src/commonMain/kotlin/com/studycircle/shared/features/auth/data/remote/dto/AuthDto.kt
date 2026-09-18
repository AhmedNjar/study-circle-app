package com.studycircle.shared.features.auth.data.remote.dto

import kotlinx.serialization.Serializable

// role is a plain String here, not the domain Role enum — DTOs shouldn't
// import domain types directly; the repository maps String <-> Role at the
// data-layer boundary (see AuthRepositoryImpl).
@Serializable
data class RegisterRequestDto(
    val username: String,
    val password: String,
    val displayName: String,
    val role: String
)

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class UserDto(val id: String, val username: String, val displayName: String, val role: String)

@Serializable
data class AuthResponseDto(val token: String, val user: UserDto)
