package com.studycircle.backend.streak

import jakarta.persistence.*
import java.time.LocalDate

// ownerKey mirrors the client's SQLDelight design exactly: a flattened,
// indexable form of the owner ("individual:<userId>", "group:<id>" once
// Phase 4 lands) alongside ownerJson for round-tripping the typed value.
@Entity
@Table(name = "streak")
@IdClass(StreakId::class)
class StreakEntity(
    @Id
    val circleId: String,

    @Id
    val ownerKey: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val ownerJson: String,

    var currentCount: Int,
    var bestCount: Int,
    var lastCompletedDate: LocalDate?,
    var freezesAvailable: Int = 0
)

data class StreakId(val circleId: String = "", val ownerKey: String = "") : java.io.Serializable
