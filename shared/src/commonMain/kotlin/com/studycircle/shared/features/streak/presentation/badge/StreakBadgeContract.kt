package com.studycircle.shared.features.streak.presentation.badge

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState

sealed interface StreakBadgeIntent : MviIntent {
    // groupId null = the logged-in user's own streak; set = that group's
    // collective streak. A personal badge never needs the caller to specify
    // whose individual streak to show — the group case does.
    data class Load(val circleId: String, val groupId: String? = null) : StreakBadgeIntent
}

data class StreakBadgeState(
    val currentCount: Int = 0,
    val bestCount: Int = 0,
    val isLoading: Boolean = false
) : MviState

sealed interface StreakBadgeEffect : MviEffect
