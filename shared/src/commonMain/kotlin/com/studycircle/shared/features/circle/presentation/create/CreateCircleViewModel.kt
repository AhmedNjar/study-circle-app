package com.studycircle.shared.features.circle.presentation.create

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.circle.domain.usecase.CreateCircleUseCase
import kotlinx.coroutines.CoroutineScope

class CreateCircleViewModel(
    private val createCircleUseCase: CreateCircleUseCase,
    scope: CoroutineScope
) : MviViewModel<CreateCircleIntent, CreateCircleState, CreateCircleEffect>(CreateCircleState(), scope) {

    override suspend fun handleIntent(intent: CreateCircleIntent) {
        when (intent) {
            is CreateCircleIntent.NameChanged -> setState { copy(name = intent.value, errorMessage = null) }
            CreateCircleIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        if (currentState.name.isBlank()) {
            setState { copy(errorMessage = "أدخل اسم الحلقة") }
            return
        }
        setState { copy(isLoading = true, errorMessage = null) }
        createCircleUseCase(currentState.name).fold(
            onSuccess = { circle ->
                setState { copy(isLoading = false) }
                emitEffect(CreateCircleEffect.NavigateToDetail(circle.id))
            },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر إنشاء الحلقة، حاول مرة أخرى") } }
        )
    }
}
