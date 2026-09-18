package com.studycircle.shared.features.streak.presentation.badge

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.streak.domain.model.StreakOwner
import com.studycircle.shared.features.streak.domain.usecase.GetStreakUseCase
import kotlinx.coroutines.CoroutineScope

class StreakBadgeViewModel(
    private val getStreakUseCase: GetStreakUseCase,
    private val authRepository: AuthRepository,
    scope: CoroutineScope
) : MviViewModel<StreakBadgeIntent, StreakBadgeState, StreakBadgeEffect>(StreakBadgeState(), scope) {

    override suspend fun handleIntent(intent: StreakBadgeIntent) {
        when (intent) {
            is StreakBadgeIntent.Load -> load(intent.circleId, intent.groupId)
        }
    }

    private suspend fun load(circleId: String, groupId: String?) {
        val owner = if (groupId != null) {
            StreakOwner.Group(groupId)
        } else {
            val userId = authRepository.currentSession()?.userId ?: return
            StreakOwner.Individual(userId)
        }
        setState { copy(isLoading = true) }
        getStreakUseCase(circleId, owner).fold(
            onSuccess = { streak -> setState { copy(isLoading = false, currentCount = streak.currentCount, bestCount = streak.bestCount) } },
            // A badge that fails to load just... doesn't show a number. Not
            // worth an error banner for a decorative gamification element.
            onFailure = { setState { copy(isLoading = false) } }
        )
    }
}
