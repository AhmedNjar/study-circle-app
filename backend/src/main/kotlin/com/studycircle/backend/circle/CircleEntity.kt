package com.studycircle.backend.circle

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "circle")
class CircleEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    val joinCode: String,

    @Column(nullable = false)
    val supervisorId: String,

    @Column(nullable = false)
    val createdAt: Instant,

    @Column(nullable = false)
    var isActive: Boolean
)

@Entity
@Table(name = "circle_member")
@IdClass(CircleMemberId::class)
class CircleMemberEntity(
    @Id
    val circleId: String,
    @Id
    val userId: String
)

data class CircleMemberId(val circleId: String = "", val userId: String = "") : java.io.Serializable
