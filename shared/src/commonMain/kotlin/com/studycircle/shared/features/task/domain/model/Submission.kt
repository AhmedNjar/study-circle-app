package com.studycircle.shared.features.task.domain.model

import kotlinx.datetime.Instant

enum class SubmissionStatus { PENDING, APPROVED, REJECTED, NEEDS_REVISION }

sealed class SubmitterRef {
    data class Individual(val userId: String) : SubmitterRef()
    data class OnBehalfOfGroup(val groupId: String, val submittedByUserId: String) : SubmitterRef()
}

// Mirrors TrackingMethod 1:1 — the use case validates evidence.type against
// the task's tracking.type before accepting a submission.
sealed class SubmissionEvidence {
    data class TimedReflection(
        val actualDurationSeconds: Long,
        val reflectionText: String,
        val countAchieved: Int? = null
    ) : SubmissionEvidence()

    data class Media(
        val localCachePath: String?,   // offline-first: written before upload
        val remoteMediaUrl: String?,   // null until sync completes
        val mediaType: MediaType,
        val durationSeconds: Int
    ) : SubmissionEvidence()

    // CountBased tasks (pages, reps, km, ayat, ...). The unit/target live on the
    // task's TrackingMethod.CountBased; evidence only carries what was achieved.
    data class Count(val countAchieved: Int) : SubmissionEvidence()

    data object CheckIn : SubmissionEvidence()
}

data class Submission(
    val id: String,
    val taskId: String,
    val submitter: SubmitterRef,
    val submittedAt: Instant,
    val status: SubmissionStatus,
    val supervisorNote: String?,
    val evidence: SubmissionEvidence
)
