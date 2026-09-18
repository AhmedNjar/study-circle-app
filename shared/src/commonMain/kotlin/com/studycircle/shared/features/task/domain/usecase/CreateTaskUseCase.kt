package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.model.NewTaskInput
import com.studycircle.shared.features.task.domain.repository.TaskRepository

class CreateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(circleId: String, input: NewTaskInput) = repository.create(circleId, input)
}
