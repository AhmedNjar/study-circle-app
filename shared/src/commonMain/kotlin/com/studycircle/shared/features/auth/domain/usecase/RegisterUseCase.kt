package com.studycircle.shared.features.auth.domain.usecase

import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String, displayName: String, role: Role) =
        repository.register(username, password, displayName, role)
}
