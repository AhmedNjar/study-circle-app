package com.studycircle.shared.features.streak.domain.repository

import com.studycircle.shared.features.streak.domain.model.Streak
import com.studycircle.shared.features.streak.domain.model.StreakOwner

interface StreakRepository {
    suspend fun getStreak(circleId: String, owner: StreakOwner): Result<Streak>
}
