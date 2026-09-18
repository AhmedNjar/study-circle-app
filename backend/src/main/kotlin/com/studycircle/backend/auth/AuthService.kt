package com.studycircle.backend.auth

import com.studycircle.backend.auth.dto.AuthResponse
import com.studycircle.backend.auth.dto.LoginRequest
import com.studycircle.backend.auth.dto.RegisterRequest
import com.studycircle.backend.auth.dto.UserSummary
import com.studycircle.backend.common.InvalidCredentialsException
import com.studycircle.backend.common.UsernameTakenException
import com.studycircle.backend.security.JwtService
import com.studycircle.backend.user.UserEntity
import com.studycircle.backend.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) throw UsernameTakenException()

        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.displayName,
            role = request.role,
            createdAt = Instant.now()
        )
        userRepository.save(user)
        return AuthResponse(
            token = jwtService.generate(user.id, user.username, user.role),
            user = user.toSummary()
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { InvalidCredentialsException() }
        if (!passwordEncoder.matches(request.password, user.passwordHash)) throw InvalidCredentialsException()

        return AuthResponse(
            token = jwtService.generate(user.id, user.username, user.role),
            user = user.toSummary()
        )
    }

    private fun UserEntity.toSummary() = UserSummary(id, username, displayName, role)
}
