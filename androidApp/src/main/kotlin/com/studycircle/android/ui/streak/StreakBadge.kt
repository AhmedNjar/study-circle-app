package com.studycircle.android.ui.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.studycircle.android.ui.mvi.rememberMviViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.streak.domain.usecase.GetStreakUseCase
import com.studycircle.shared.features.streak.presentation.badge.StreakBadgeIntent
import com.studycircle.shared.features.streak.presentation.badge.StreakBadgeState
import com.studycircle.shared.features.streak.presentation.badge.StreakBadgeViewModel
import com.studycircle.shared.localization.ArabicTerms
import org.koin.compose.koinInject
import androidx.compose.ui.tooling.preview.Preview

private val Gold = Color(0xFFEF9F27)
private val GoldText = Color(0xFF854F0B) // amber-800 — readable on the pale gold tint below

// Deliberately not a flame icon (the Duolingo "streak" convention) —
// الرِّباط means "the tie/bond", not "streak fire", so this reuses the
// gold-accent + small geometric-dot motif from the login screen instead of
// borrowing iconography that doesn't match what the word means.
@Composable
fun StreakBadge(circleId: String, groupId: String? = null, modifier: Modifier = Modifier) {
    val getStreakUseCase: GetStreakUseCase = koinInject()
    val authRepository: AuthRepository = koinInject()
    val viewModel = rememberMviViewModel(key = "$circleId/$groupId") { scope -> StreakBadgeViewModel(getStreakUseCase, authRepository, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(circleId, groupId) { viewModel.dispatch(StreakBadgeIntent.Load(circleId, groupId)) }

    StreakBadgeContent(state = state, modifier = modifier)
}

@Composable
fun StreakBadgeContent(state: StreakBadgeState, modifier: Modifier = Modifier) {
    if (state.isLoading && state.currentCount == 0) return // nothing worth showing yet

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Gold.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(Gold)
        )
        Text(
            "${state.currentCount} ${ArabicTerms.STREAK_LABEL}",
            fontWeight = FontWeight.Medium,
            color = GoldText,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StreakBadgePreview() {
    MaterialTheme {
        StreakBadgeContent(
            state = StreakBadgeState(currentCount = 12)
        )
    }
}
