package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.repository.TaskRepository

class GetTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) = repository.getById(taskId)
}
