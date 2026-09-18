package com.studycircle.shared.features.circle.presentation.join

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState

sealed interface JoinCircleIntent : MviIntent {
    data class CodeChanged(val value: String) : JoinCircleIntent
    data object Submit : JoinCircleIntent
}

data class JoinCircleState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface JoinCircleEffect : MviEffect {
    data class NavigateToDetail(val circleId: String) : JoinCircleEffect
}
