package com.studycircle.shared.features.home.presentation

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.auth.domain.usecase.LogoutUseCase
import com.studycircle.shared.features.home.domain.usecase.GetHomeDashboardUseCase
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.usecase.ReviewSubmissionUseCase
import com.studycircle.shared.features.task.domain.usecase.SyncPendingSubmissionsUseCase
import kotlinx.coroutines.CoroutineScope

class HomeViewModel(
    private val getHomeDashboard: GetHomeDashboardUseCase,
    private val reviewSubmission: ReviewSubmissionUseCase,
    private val syncPendingSubmissions: SyncPendingSubmissionsUseCase,
    private val logout: LogoutUseCase,
    scope: CoroutineScope
) : MviViewModel<HomeIntent, HomeState, HomeEffect>(HomeState(), scope) {

    override suspend fun handleIntent(intent: HomeIntent) {
        when (intent) {
            // Load is dispatched on every screen resume (so returning from "create
            // circle" / "submit task" shows fresh data) and Refresh from the retry
            // button — same work, the difference is only who asks.
            HomeIntent.Load, HomeIntent.Refresh -> load()
            is HomeIntent.Approve -> review(intent.submissionId, SubmissionStatus.APPROVED, ReviewDecision.APPROVED)
            is HomeIntent.Reject -> review(intent.submissionId, SubmissionStatus.REJECTED, ReviewDecision.REJECTED)
            // The session Flow observed by the nav layer takes it from here.
            HomeIntent.Logout -> logout()
        }
    }

    private var loadInFlight = false

    private suspend fun load() {
        if (loadInFlight) return
        loadInFlight = true
        // Only blank the screen for the very first load; later refreshes keep the
        // current dashboard visible and swap it in when the new one arrives.
        setState { copy(isLoading = dashboard == null, errorMessage = null) }

        // Push any submissions queued while offline before reading, so the numbers
        // include them. Failure is irrelevant here — they stay queued for next time.
        runCatching { syncPendingSubmissions() }

        val result = try {
            getHomeDashboard()
        } finally {
            loadInFlight = false
        }
        result.fold(
            onSuccess = { dashboard ->
                setState { copy(isLoading = false, dashboard = dashboard, decisions = emptyMap(), reviewsInFlight = emptySet()) }
            },
            onFailure = {
                // Keep showing the previous dashboard (if any) under the error.
                setState { copy(isLoading = false, errorMessage = "تعذر تحميل الصفحة الرئيسية") }
            }
        )
    }

    private suspend fun review(submissionId: String, status: SubmissionStatus, decision: ReviewDecision) {
        if (submissionId in currentState.reviewsInFlight || submissionId in currentState.decisions) return
        setState { copy(reviewsInFlight = reviewsInFlight + submissionId) }
        reviewSubmission(submissionId, status, null).fold(
            onSuccess = {
                setState { copy(reviewsInFlight = reviewsInFlight - submissionId, decisions = decisions + (submissionId to decision)) }
            },
            onFailure = {
                setState { copy(reviewsInFlight = reviewsInFlight - submissionId) }
                emitEffect(HomeEffect.ShowMessage("تعذر تحديث حالة التسليم"))
            }
        )
    }
}
