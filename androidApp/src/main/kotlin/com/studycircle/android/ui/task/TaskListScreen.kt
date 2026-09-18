package com.studycircle.android.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studycircle.shared.features.task.domain.usecase.ListTasksUseCase
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import kotlinx.datetime.LocalDate
import com.studycircle.shared.features.task.presentation.tasklist.TaskListIntent
import com.studycircle.shared.features.task.presentation.tasklist.TaskListViewModel
import com.studycircle.shared.localization.ArabicTerms
import org.koin.compose.koinInject

@Composable
fun TaskListScreen(
    circleId: String,
    onCreateTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onReviewTask: (taskId: String) -> Unit
) {
    val useCase: ListTasksUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { TaskListViewModel(useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(circleId) { viewModel.dispatch(TaskListIntent.Load(circleId)) }

    TaskListContent(
        state = state,
        onCreateTask = onCreateTask,
        onOpenTask = onOpenTask,
        onReviewTask = onReviewTask
    )
}

@Composable
fun TaskListContent(
    state: com.studycircle.shared.features.task.presentation.tasklist.TaskListState,
    onCreateTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onReviewTask: (taskId: String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) { Text("+") }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.tasks.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد مهام بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tasks) { task ->
                        Card {
                            Column(Modifier.padding(16.dp)) {
                                Text(task.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${ArabicTerms.activityTypeLabel(task.activityType)} · ${task.dueDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onOpenTask(task.id) }) { Text("ابدأ") }
                                    TextButton(onClick = { onReviewTask(task.id) }) { Text("مراجعة") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TaskListPreview() {
    MaterialTheme {
        TaskListContent(
            state = com.studycircle.shared.features.task.presentation.tasklist.TaskListState(
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
            onReviewTask = {}
        )
    }
}
