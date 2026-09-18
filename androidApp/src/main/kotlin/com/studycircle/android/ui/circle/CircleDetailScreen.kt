package com.studycircle.android.ui.circle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.shared.features.circle.domain.model.CircleDetail
import com.studycircle.shared.features.circle.domain.model.CircleMemberSummary
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import kotlinx.datetime.Clock
import com.studycircle.shared.features.circle.presentation.detail.CircleDetailIntent
import com.studycircle.shared.features.circle.presentation.detail.CircleDetailViewModel
import com.studycircle.shared.localization.ArabicTerms
import org.koin.compose.koinInject

@Composable
fun CircleDetailScreen(circleId: String, onViewTasks: () -> Unit) {
    val useCase: GetCircleUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { CircleDetailViewModel(useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(circleId) {
        viewModel.dispatch(CircleDetailIntent.Load(circleId))
    }

    CircleDetailContent(state = state, onViewTasks = onViewTasks)
}

@Composable
fun CircleDetailContent(
    state: com.studycircle.shared.features.circle.presentation.detail.CircleDetailState,
    onViewTasks: () -> Unit
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        state.circle != null -> {
            val circle = state.circle!!
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Text(circle.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text("رمز الانضمام: ${circle.joinCode}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(text = "عرض المهام", onClick = onViewTasks)
                Spacer(Modifier.height(16.dp))
                Text("الأعضاء", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(circle.members) { member ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(member.displayName)
                            Text(ArabicTerms.roleLabel(member.role), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CircleDetailPreview() {
    MaterialTheme {
        CircleDetailContent(
            state = com.studycircle.shared.features.circle.presentation.detail.CircleDetailState(
                circle = CircleDetail(
                    id = "1",
                    name = "حلقة القرآن",
                    joinCode = "ABC123",
                    supervisorId = "sup1",
                    members = listOf(
                        CircleMemberSummary("1", "أحمد", com.studycircle.shared.features.auth.domain.model.Role.SUPERVISOR),
                        CircleMemberSummary("2", "محمد", com.studycircle.shared.features.auth.domain.model.Role.STUDENT)
                    ),
                    createdAt = Clock.System.now(),
                    isActive = true
                )
            ),
            onViewTasks = {}
        )
    }
}
