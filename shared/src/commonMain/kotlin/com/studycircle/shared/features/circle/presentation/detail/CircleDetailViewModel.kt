package com.studycircle.shared.features.circle.presentation.detail

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import kotlinx.coroutines.CoroutineScope

class CircleDetailViewModel(
    private val getCircleUseCase: GetCircleUseCase,
    scope: CoroutineScope
) : MviViewModel<CircleDetailIntent, CircleDetailState, CircleDetailEffect>(CircleDetailState(), scope) {

    override suspend fun handleIntent(intent: CircleDetailIntent) {
        when (intent) {
            is CircleDetailIntent.Load -> load(intent.circleId)
        }
    }

    private suspend fun load(circleId: String) {
        setState { copy(isLoading = true, errorMessage = null) }
        getCircleUseCase(circleId).fold(
            onSuccess = { detail -> setState { copy(isLoading = false, circle = detail) } },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر تحميل بيانات الحلقة") } }
        )
    }
}
