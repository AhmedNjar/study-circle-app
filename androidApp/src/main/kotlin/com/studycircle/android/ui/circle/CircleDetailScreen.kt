package com.studycircle.android.ui.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.IslamicTag
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.android.ui.components.SectionTitle
import com.studycircle.android.ui.streak.StreakBadge
import com.studycircle.android.ui.streak.StreakBadgeContent
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.circle.domain.model.CircleDetail
import com.studycircle.shared.features.circle.domain.model.CircleMemberSummary
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import com.studycircle.shared.features.circle.presentation.detail.CircleDetailIntent
import com.studycircle.shared.features.circle.presentation.detail.CircleDetailState
import com.studycircle.shared.features.circle.presentation.detail.CircleDetailViewModel
import com.studycircle.shared.features.streak.presentation.badge.StreakBadgeState
import com.studycircle.shared.localization.ArabicTerms
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CircleDetailScreen(circleId: String, onViewTasks: () -> Unit) {
    val useCase: GetCircleUseCase = koinInject()
    val viewModel = rememberMviViewModel { scope -> CircleDetailViewModel(useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(circleId) {
        viewModel.dispatch(CircleDetailIntent.Load(circleId))
    }

    StudyCircleTheme {
        CircleDetailContent(
            state = state,
            onViewTasks = onViewTasks
        )
    }
}

@Composable
fun CircleDetailContent(
    state: CircleDetailState,
    onViewTasks: () -> Unit,
    streakBadge: @Composable (String) -> Unit = { id -> StreakBadge(circleId = id) }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        IslamicBackground(Modifier.fillMaxSize())

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            state.circle != null -> {
                val circle = state.circle!!
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    // Header Card
                    PaperCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = circle.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                // Placeholder for "Ribat" count if available, or just the badge
                                streakBadge(circle.id)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "رمز الانضمام: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                IslamicTag(text = circle.joinCode)
                            }
                            Spacer(Modifier.height(20.dp))
                            PrimaryButton(text = "عرض المهام", onClick = onViewTasks)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    SectionTitle(title = "الأعضاء")
                    Spacer(Modifier.height(12.dp))

                    PaperCard(modifier = Modifier.fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(circle.members) { member ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (member.role == Role.SUPERVISOR) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.secondary
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.displayName.take(1),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.size(12.dp))
                                        Text(
                                            text = member.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    IslamicTag(
                                        text = ArabicTerms.roleLabel(member.role),
                                        color = if (member.role == Role.SUPERVISOR) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircleDetailContentPreview() {
    val mockCircle = CircleDetail(
        id = "1",
        name = "حلقة تجريبية",
        joinCode = "ABCDEF",
        supervisorId = "u1",
        members = listOf(
            CircleMemberSummary("u1", "أحمد السيد", Role.SUPERVISOR),
            CircleMemberSummary("u2", "محمد علي", Role.STUDENT),
            CircleMemberSummary("u3", "سارة خالد", Role.STUDENT)
        ),
        createdAt = Clock.System.now(),
        isActive = true
    )
    StudyCircleTheme {
        CircleDetailContent(
            state = CircleDetailState(circle = mockCircle),
            onViewTasks = {},
            streakBadge = { StreakBadgeContent(state = StreakBadgeState(currentCount = 7)) }
        )
    }
}
