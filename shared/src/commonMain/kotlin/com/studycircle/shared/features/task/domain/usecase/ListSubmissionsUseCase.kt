package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.repository.SubmissionRepository

class ListSubmissionsUseCase(private val repository: SubmissionRepository) {
    suspend operator fun invoke(taskId: String, status: SubmissionStatus? = null) = repository.listByTask(taskId, status)
}
