package com.studycircle.shared.features.task.presentation.tasklist

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.task.domain.usecase.ListTasksUseCase
import kotlinx.coroutines.CoroutineScope

class TaskListViewModel(
    private val listTasksUseCase: ListTasksUseCase,
    scope: CoroutineScope
) : MviViewModel<TaskListIntent, TaskListState, TaskListEffect>(TaskListState(), scope) {

    override suspend fun handleIntent(intent: TaskListIntent) {
        when (intent) {
            is TaskListIntent.Load -> load(intent.circleId)
        }
    }

    private suspend fun load(circleId: String) {
        setState { copy(isLoading = true, errorMessage = null, circleId = circleId) }
        listTasksUseCase(circleId).fold(
            onSuccess = { tasks -> setState { copy(isLoading = false, tasks = tasks) } },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر تحميل المهام") } }
        )
    }
}
