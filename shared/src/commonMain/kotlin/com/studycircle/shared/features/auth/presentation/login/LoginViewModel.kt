package com.studycircle.shared.features.auth.presentation.login

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.core.network.ApiException
import com.studycircle.shared.features.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.CoroutineScope

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    scope: CoroutineScope
) : MviViewModel<LoginIntent, LoginState, LoginEffect>(LoginState(), scope) {

    override suspend fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> setState { copy(username = intent.value, errorMessage = null) }
            is LoginIntent.PasswordChanged -> setState { copy(password = intent.value, errorMessage = null) }
            LoginIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            setState { copy(errorMessage = "أدخل اسم المستخدم وكلمة المرور") }
            return
        }
        setState { copy(isLoading = true, errorMessage = null) }
        loginUseCase(currentState.username, currentState.password).fold(
            onSuccess = {
                setState { copy(isLoading = false) }
                emitEffect(LoginEffect.NavigateToCircleList)
            },
            onFailure = { error -> setState { copy(isLoading = false, errorMessage = errorMessageFor(error)) } }
        )
    }

    private fun errorMessageFor(error: Throwable): String =
        when ((error as? ApiException)?.code) {
            "INVALID_CREDENTIALS" -> "اسم المستخدم أو كلمة المرور غير صحيحة"
            else -> "تعذر تسجيل الدخول، حاول مرة أخرى"
        }
}
