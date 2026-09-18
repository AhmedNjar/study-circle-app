package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.repository.SubmissionRepository

class ReviewSubmissionUseCase(private val repository: SubmissionRepository) {
    suspend operator fun invoke(submissionId: String, status: SubmissionStatus, note: String?) =
        repository.review(submissionId, status, note)
}
