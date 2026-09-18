package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.repository.TaskGroupRepository

class ListTaskGroupsUseCase(private val repository: TaskGroupRepository) {
    suspend operator fun invoke(taskId: String) = repository.listForTask(taskId)
}
