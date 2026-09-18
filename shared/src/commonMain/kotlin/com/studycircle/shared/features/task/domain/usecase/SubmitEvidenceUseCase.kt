package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.repository.SubmissionRepository

class SubmitEvidenceUseCase(private val repository: SubmissionRepository) {
    suspend operator fun invoke(taskId: String, evidence: SubmissionEvidence) = repository.submit(taskId, evidence)
}
