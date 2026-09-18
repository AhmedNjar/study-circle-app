package com.studycircle.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val decoded = jwtService.decode(header.removePrefix("Bearer "))
            if (decoded != null) {
                val principal = AuthenticatedUser(decoded.userId, decoded.username, decoded.role)
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${decoded.role.name}"))
                val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
            // An invalid/expired token just leaves the request unauthenticated —
            // SecurityConfig's authorizeHttpRequests then rejects it with 401/403,
            // no special-casing needed here.
        }
        filterChain.doFilter(request, response)
    }
}
