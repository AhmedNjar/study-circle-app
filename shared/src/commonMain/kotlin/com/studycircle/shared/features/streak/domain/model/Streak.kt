package com.studycircle.shared.features.streak.domain.model

import kotlinx.datetime.LocalDate

sealed class StreakOwner {
    data class Individual(val userId: String) : StreakOwner()
    data class Group(val groupId: String) : StreakOwner()
}

data class Streak(
    val circleId: String,
    val owner: StreakOwner,   // الرِّباط now belongs to a person OR a group
    val currentCount: Int,
    val bestCount: Int,
    val lastCompletedDate: LocalDate?,
    val freezesAvailable: Int = 0   // grace days — flag for post-MVP
)
