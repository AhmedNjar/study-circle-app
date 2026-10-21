package com.studycircle.shared.features.auth.domain.usecase

import com.studycircle.shared.features.auth.domain.model.Session
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

// Emits the persisted session (null = signed out). Drives start-destination
// selection and automatic return-to-login after logout.
class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Session?> = repository.observeSession()
}
