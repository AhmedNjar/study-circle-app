package com.studycircle.shared.features.auth.domain.usecase

import com.studycircle.shared.features.auth.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}
