package com.studycircle.backend.task

import jakarta.persistence.*
import java.time.LocalDate

// tracking/audience/metadata are stored as JSON TEXT rather than normalized
// columns — same call as the client's SQLDelight Task table, for the same
// reason: they're genuinely variant-shaped (sealed-class-like), and a local
// cache/row doesn't need to query inside them, just round-trip them intact.
@Entity
@Table(name = "task")
class TaskEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val circleId: String,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val dueDate: LocalDate,

    @Column(nullable = false)
    val createdBy: String,

    @Column(nullable = false)
    val activityType: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val trackingJson: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val audienceJson: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val metadataJson: String
)
