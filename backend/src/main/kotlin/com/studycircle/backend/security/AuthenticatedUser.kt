package com.studycircle.backend.security

import com.studycircle.backend.user.Role
import org.springframework.security.core.context.SecurityContextHolder

// Set as the Authentication#principal by JwtAuthFilter. Controllers/services
// read it via SecurityContextHolder rather than re-decoding the token.
data class AuthenticatedUser(val userId: String, val username: String, val role: Role)

object CurrentUser {
    fun get(): AuthenticatedUser =
        SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
}
