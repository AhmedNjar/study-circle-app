package com.studycircle.shared.features.circle.domain.usecase

import com.studycircle.shared.features.circle.domain.repository.CircleRepository

class JoinCircleUseCase(private val repository: CircleRepository) {
    suspend operator fun invoke(joinCode: String) = repository.join(joinCode)
}
