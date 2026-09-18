package com.studycircle.shared.features.circle.presentation.detail

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.circle.domain.model.CircleDetail

sealed interface CircleDetailIntent : MviIntent {
    data class Load(val circleId: String) : CircleDetailIntent
}

data class CircleDetailState(
    val circle: CircleDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface CircleDetailEffect : MviEffect
