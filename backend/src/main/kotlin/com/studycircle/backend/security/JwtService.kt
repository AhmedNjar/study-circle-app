package com.studycircle.backend.security

import com.studycircle.backend.user.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

data class DecodedToken(val userId: String, val username: String, val role: Role)

@Component
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-minutes:1440}") private val expirationMinutes: Long
) {
    // HS256 needs a >=256-bit secret — jwt.secret in application.yml must be a
    // long random string in any real deployment, not the placeholder there now.
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generate(userId: String, username: String, role: Role): String {
        val now = Date()
        val expiry = Date(now.time + expirationMinutes * 60_000)
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    // Returns null on any invalid/expired token rather than throwing — the
    // filter turns that into "unauthenticated", not a 500.
    fun decode(token: String): DecodedToken? = try {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        DecodedToken(
            userId = claims.subject,
            username = claims.get("username", String::class.java),
            role = Role.valueOf(claims.get("role", String::class.java))
        )
    } catch (e: Exception) {
        null
    }
}
