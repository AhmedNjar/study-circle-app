package com.studycircle.android.ui.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.studycircle.android.ui.mvi.rememberMviViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.activityEmoji
import com.studycircle.android.ui.components.activityLabel
import com.studycircle.android.ui.components.startLabel
import com.studycircle.android.ui.components.toArabicLong
import com.studycircle.android.ui.streak.StreakBadge
import com.studycircle.android.ui.streak.StreakBadgeContent
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.shared.features.streak.presentation.badge.StreakBadgeState
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.ListTasksUseCase
import com.studycircle.shared.features.task.presentation.tasklist.TaskListIntent
import com.studycircle.shared.features.task.presentation.tasklist.TaskListState
import com.studycircle.shared.features.task.presentation.tasklist.TaskListViewModel
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

@Composable
fun TaskListScreen(
    circleId: String,
    isSupervisor: Boolean,
    onCreateTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onReviewTask: (taskId: String) -> Unit
) {
    val useCase: ListTasksUseCase = koinInject()
    val viewModel = rememberMviViewModel { scope -> TaskListViewModel(useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(circleId) { viewModel.dispatch(TaskListIntent.Load(circleId)) }

    StudyCircleTheme {
        TaskListContent(
            circleId = circleId,
            state = state,
            isSupervisor = isSupervisor,
            onCreateTask = onCreateTask,
            onOpenTask = onOpenTask,
            onReviewTask = onReviewTask
        )
    }
}

@Composable
fun TaskListContent(
    circleId: String,
    state: TaskListState,
    isSupervisor: Boolean,
    onCreateTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onReviewTask: (taskId: String) -> Unit,
    streakBadge: @Composable () -> Unit = { StreakBadge(circleId = circleId) }
) {
    Scaffold(
        // Only supervisors create tasks (the backend enforces it too).
        floatingActionButton = {
            if (isSupervisor) {
                FloatingActionButton(
                    onClick = onCreateTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            IslamicBackground(Modifier.fillMaxSize())

            Column(Modifier.fillMaxSize()) {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    streakBadge()
                }

                if (state.isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (state.tasks.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "لا توجد مهام نشطة حالياً",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.tasks, key = { it.id }) { task ->
                            IslamicTaskCard(
                                task = task,
                                isSupervisor = isSupervisor,
                                onOpenTask = { onOpenTask(task.id) },
                                onReviewTask = { onReviewTask(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IslamicTaskCard(
    task: CircleTask,
    isSupervisor: Boolean,
    onOpenTask: () -> Unit,
    onReviewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    PaperCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
    ) {
        Box(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Gold accent "glow" line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .align(Alignment.CenterEnd)
            )

            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${activityLabel(task)} • ${task.dueDate.toArabicLong()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(activityEmoji(task.activityType), style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSupervisor) {
                        TextButton(
                            onClick = onReviewTask,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("مراجعة", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.size(8.dp))
                    }
                    TextButton(
                        onClick = onOpenTask,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(startLabel(task.activityType), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TaskListPreview() {
    StudyCircleTheme {
        TaskListContent(
            circleId = "c1",
            isSupervisor = true,
            state = TaskListState(
                tasks = listOf(
                    CircleTask(
                        id = "1",
                        circleId = "c1",
                        title = "قراءة سورة البقرة",
                        activityType = ActivityType.READING,
                        dueDate = LocalDate(2023, 10, 1),
                        createdBy = "sup1",
                        tracking = TrackingMethod.ManualCheckIn,
                        audience = TaskAudience.AllMembers
                    ),
                    CircleTask(
                        id = "2",
                        circleId = "c1",
                        title = "تسميع جزء عم",
                        activityType = ActivityType.RECITATION,
                        dueDate = LocalDate(2023, 10, 5),
                        createdBy = "sup1",
                        tracking = TrackingMethod.MediaSubmission(setOf(MediaType.AUDIO)),
                        audience = TaskAudience.AllMembers
                    )
                )
            ),
            onCreateTask = {},
            onOpenTask = {},
            onReviewTask = {},
            streakBadge = {
                StreakBadgeContent(state = StreakBadgeState(currentCount = 5))
            }
        )
    }
}
