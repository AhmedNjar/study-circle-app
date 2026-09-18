package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.repository.TaskRepository

class ListTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(circleId: String) = repository.listByCircle(circleId)
}
