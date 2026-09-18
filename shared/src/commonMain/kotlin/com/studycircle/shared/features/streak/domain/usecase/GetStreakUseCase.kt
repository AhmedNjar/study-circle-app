package com.studycircle.shared.features.streak.domain.usecase

import com.studycircle.shared.features.streak.domain.model.StreakOwner
import com.studycircle.shared.features.streak.domain.repository.StreakRepository

class GetStreakUseCase(private val repository: StreakRepository) {
    suspend operator fun invoke(circleId: String, owner: StreakOwner) = repository.getStreak(circleId, owner)
}
