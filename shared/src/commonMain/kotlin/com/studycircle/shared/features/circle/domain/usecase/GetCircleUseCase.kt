package com.studycircle.shared.features.circle.domain.usecase

import com.studycircle.shared.features.circle.domain.repository.CircleRepository

class GetCircleUseCase(private val repository: CircleRepository) {
    suspend operator fun invoke(circleId: String) = repository.getDetail(circleId)
}
