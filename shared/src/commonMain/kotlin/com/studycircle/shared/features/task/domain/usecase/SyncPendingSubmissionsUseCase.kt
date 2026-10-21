package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.repository.SubmissionRepository

// Retries every locally-queued submission that never reached the server.
// Returns how many were synced. Cheap when there's nothing pending, so callers
// can invoke it opportunistically (app start / home refresh) until a real
// background worker (WorkManager / BGTaskScheduler) replaces that trigger.
class SyncPendingSubmissionsUseCase(private val repository: SubmissionRepository) {
    suspend operator fun invoke(): Result<Int> = repository.syncPending()
}
