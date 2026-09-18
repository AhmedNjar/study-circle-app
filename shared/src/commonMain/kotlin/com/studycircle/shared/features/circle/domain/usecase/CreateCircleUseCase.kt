package com.studycircle.shared.features.circle.domain.usecase

import com.studycircle.shared.features.circle.domain.repository.CircleRepository

class CreateCircleUseCase(private val repository: CircleRepository) {
    suspend operator fun invoke(name: String) = repository.create(name)
}
