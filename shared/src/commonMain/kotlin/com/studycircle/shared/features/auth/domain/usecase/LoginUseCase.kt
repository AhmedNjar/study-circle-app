package com.studycircle.shared.features.auth.domain.usecase

import com.studycircle.shared.features.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String) =
        repository.login(username, password)
}
