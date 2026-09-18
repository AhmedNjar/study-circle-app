package com.studycircle.shared.features.task.presentation.tasklist

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.task.domain.model.CircleTask

sealed interface TaskListIntent : MviIntent {
    data class Load(val circleId: String) : TaskListIntent
}

data class TaskListState(
    val circleId: String? = null,
    val tasks: List<CircleTask> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface TaskListEffect : MviEffect
