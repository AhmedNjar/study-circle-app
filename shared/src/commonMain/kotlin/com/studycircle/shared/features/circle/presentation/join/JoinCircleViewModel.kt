package com.studycircle.shared.features.circle.presentation.join

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.core.network.ApiException
import com.studycircle.shared.features.circle.domain.usecase.JoinCircleUseCase
import kotlinx.coroutines.CoroutineScope

class JoinCircleViewModel(
    private val joinCircleUseCase: JoinCircleUseCase,
    scope: CoroutineScope
) : MviViewModel<JoinCircleIntent, JoinCircleState, JoinCircleEffect>(JoinCircleState(), scope) {

    override suspend fun handleIntent(intent: JoinCircleIntent) {
        when (intent) {
            is JoinCircleIntent.CodeChanged -> setState { copy(code = intent.value.uppercase(), errorMessage = null) }
            JoinCircleIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        if (currentState.code.isBlank()) {
            setState { copy(errorMessage = "أدخل رمز الحلقة") }
            return
        }
        setState { copy(isLoading = true, errorMessage = null) }
        joinCircleUseCase(currentState.code).fold(
            onSuccess = { circle ->
                setState { copy(isLoading = false) }
                emitEffect(JoinCircleEffect.NavigateToDetail(circle.id))
            },
            onFailure = { error -> setState { copy(isLoading = false, errorMessage = errorMessageFor(error)) } }
        )
    }

    private fun errorMessageFor(error: Throwable): String =
        when ((error as? ApiException)?.code) {
            "CIRCLE_NOT_FOUND" -> "لا توجد حلقة بهذا الرمز"
            "ALREADY_MEMBER" -> "أنت عضو في هذه الحلقة بالفعل"
            "CIRCLE_INACTIVE" -> "هذه الحلقة لم تعد نشطة"
            else -> "تعذر الانضمام، حاول مرة أخرى"
        }
}
