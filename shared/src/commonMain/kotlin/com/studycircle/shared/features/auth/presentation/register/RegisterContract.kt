package com.studycircle.shared.features.auth.presentation.register

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.auth.domain.model.Role

sealed interface RegisterIntent : MviIntent {
    data class DisplayNameChanged(val value: String) : RegisterIntent
    data class UsernameChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class RoleChanged(val value: Role) : RegisterIntent
    data object Submit : RegisterIntent
}

data class RegisterState(
    val displayName: String = "",
    val username: String = "",
    val password: String = "",
    val role: Role = Role.STUDENT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface RegisterEffect : MviEffect {
    data object NavigateToCircleList : RegisterEffect
}
