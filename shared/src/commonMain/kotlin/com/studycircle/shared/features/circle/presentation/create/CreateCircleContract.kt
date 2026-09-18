package com.studycircle.shared.features.circle.presentation.create

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState

sealed interface CreateCircleIntent : MviIntent {
    data class NameChanged(val value: String) : CreateCircleIntent
    data object Submit : CreateCircleIntent
}

data class CreateCircleState(
    val name: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface CreateCircleEffect : MviEffect {
    data class NavigateToDetail(val circleId: String) : CreateCircleEffect
}
