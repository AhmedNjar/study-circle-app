package com.studycircle.backend.submission

import jakarta.persistence.*
import java.time.Instant

enum class SubmissionStatus { PENDING, APPROVED, REJECTED, NEEDS_REVISION }

// submitterJson (not a flat submitterId column) for the same reason as
// Task's tracking/audience — Phase 2 only ever writes an Individual here,
// but Phase 4's OnBehalfOfGroup variant slots in as a new JSON shape rather
// than a schema migration.
@Entity
@Table(name = "submission")
class SubmissionEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val taskId: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val submitterJson: String,

    @Column(nullable = false)
    val submittedAt: Instant,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SubmissionStatus,

    var supervisorNote: String? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    val evidenceJson: String
)
