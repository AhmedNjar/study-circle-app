package com.studycircle.shared.features.auth.presentation.register

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.core.network.ApiException
import com.studycircle.shared.features.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.CoroutineScope

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    scope: CoroutineScope
) : MviViewModel<RegisterIntent, RegisterState, RegisterEffect>(RegisterState(), scope) {

    override suspend fun handleIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.DisplayNameChanged -> setState { copy(displayName = intent.value, errorMessage = null) }
            is RegisterIntent.UsernameChanged -> setState { copy(username = intent.value, errorMessage = null) }
            is RegisterIntent.PasswordChanged -> setState { copy(password = intent.value, errorMessage = null) }
            is RegisterIntent.RoleChanged -> setState { copy(role = intent.value) }
            RegisterIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        val s = currentState
        if (s.displayName.isBlank() || s.username.isBlank() || s.password.isBlank()) {
            setState { copy(errorMessage = "يرجى إكمال جميع الحقول") }
            return
        }
        setState { copy(isLoading = true, errorMessage = null) }
        registerUseCase(s.username, s.password, s.displayName, s.role).fold(
            onSuccess = {
                setState { copy(isLoading = false) }
                emitEffect(RegisterEffect.NavigateToCircleList)
            },
            onFailure = { error -> setState { copy(isLoading = false, errorMessage = errorMessageFor(error)) } }
        )
    }

    private fun errorMessageFor(error: Throwable): String =
        when ((error as? ApiException)?.code) {
            "USERNAME_TAKEN" -> "اسم المستخدم هذا مستخدم بالفعل"
            else -> "تعذر إنشاء الحساب، حاول مرة أخرى"
        }
}
