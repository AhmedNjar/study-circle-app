package com.studycircle.shared.features.home.presentation

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.home.domain.model.HomeDashboard

sealed interface HomeIntent : MviIntent {
    data object Load : HomeIntent
    data object Refresh : HomeIntent
    data class Approve(val submissionId: String) : HomeIntent
    data class Reject(val submissionId: String) : HomeIntent
    data object Logout : HomeIntent
}

enum class ReviewDecision { APPROVED, REJECTED }

data class HomeState(
    val isLoading: Boolean = true,
    val dashboard: HomeDashboard? = null,
    val errorMessage: String? = null,
    // Decisions made this session: the card flips to a "resolved" row instead of
    // vanishing (the redesigned dashboard's behaviour), and survives until the
    // next full reload from the server.
    val decisions: Map<String, ReviewDecision> = emptyMap(),
    // Submissions whose review request is currently in flight — disables
    // double-taps on approve/reject.
    val reviewsInFlight: Set<String> = emptySet()
) : MviState {
    val pendingReviewCount: Int
        get() = dashboard?.pendingReviews?.count { it.submissionId !in decisions } ?: 0
}

sealed interface HomeEffect : MviEffect {
    data class ShowMessage(val text: String) : HomeEffect
}
