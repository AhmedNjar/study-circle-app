package com.studycircle.shared.features.task.domain.repository

import com.studycircle.shared.features.task.domain.model.Submission
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.SubmissionStatus

interface SubmissionRepository {
    suspend fun submit(taskId: String, evidence: SubmissionEvidence): Result<Submission>
    suspend fun listByTask(taskId: String, status: SubmissionStatus? = null): Result<List<Submission>>
    suspend fun review(submissionId: String, status: SubmissionStatus, note: String?): Result<Submission>

    // Retries every locally-queued submission that hasn't reached the server
    // yet (syncedRemotely = 0) — this IS the offline-first sync worker for
    // Phase 2. It's manually triggered (app resume, a "sync now" action) here
    // rather than a real WorkManager/BGTaskScheduler job; see
    // GETTING_STARTED.md for why that's a deliberate Phase 2 scope line, not
    // an oversight. Returns how many submissions were successfully synced.
    suspend fun syncPending(): Result<Int>
}
