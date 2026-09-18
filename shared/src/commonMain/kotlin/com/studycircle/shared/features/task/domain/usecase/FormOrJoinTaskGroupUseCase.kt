package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.repository.TaskGroupRepository

class FormOrJoinTaskGroupUseCase(private val repository: TaskGroupRepository) {
    suspend operator fun invoke(taskId: String, groupId: String?, name: String?) =
        repository.formOrJoin(taskId, groupId, name)
}
