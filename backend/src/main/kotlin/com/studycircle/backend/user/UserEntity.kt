package com.studycircle.backend.user

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "app_user")
class UserEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val displayName: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role,

    @Column(nullable = false)
    val createdAt: Instant
)

enum class Role { SUPERVISOR, STUDENT }
