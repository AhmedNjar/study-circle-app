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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.studycircle.shared.features.task.domain.model.Submission
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.SubmitterRef
import com.studycircle.shared.features.task.domain.usecase.ListSubmissionsUseCase
import com.studycircle.shared.features.task.domain.usecase.ReviewSubmissionUseCase
import com.studycircle.shared.features.task.presentation.review.ReviewQueueIntent
import com.studycircle.shared.features.task.presentation.review.ReviewQueueViewModel
import org.koin.compose.koinInject

import androidx.compose.ui.tooling.preview.Preview
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.presentation.review.ReviewQueueState
import kotlinx.datetime.Clock

@Composable
fun ReviewQueueScreen(taskId: String) {
    val listUseCase: ListSubmissionsUseCase = koinInject()
    val reviewUseCase: ReviewSubmissionUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { ReviewQueueViewModel(listUseCase, reviewUseCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(taskId) { viewModel.dispatch(ReviewQueueIntent.Load(taskId)) }

    ReviewQueueContent(
        state = state,
        onIntent = { viewModel.dispatch(it) }
    )
}

@Composable
fun ReviewQueueContent(
    state: ReviewQueueState,
    onIntent: (ReviewQueueIntent) -> Unit
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.submissions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد مهام بانتظار المراجعة", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.submissions) { submission ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            submitterSummary(submission.submitter),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(evidenceSummary(submission), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onIntent(ReviewQueueIntent.Approve(submission.id)) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("قبول") }
                            OutlinedButton(onClick = { onIntent(ReviewQueueIntent.Reject(submission.id)) }) {
                                Text("رفض")
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
fun ReviewQueueContentPreview() {
    MaterialTheme {
        ReviewQueueContent(
            state = ReviewQueueState(
                submissions = listOf(
                    Submission(
                        id = "1",
                        taskId = "task1",
                        submitter = SubmitterRef.Individual("u1"),
                        submittedAt = Clock.System.now(),
                        status = SubmissionStatus.PENDING,
                        supervisorNote = null,
                        evidence = SubmissionEvidence.CheckIn
                    )
                )
            ),
            onIntent = {}
        )
    }
}

// Shows the raw userId/groupId rather than a resolved display name — the
// review queue doesn't currently load circle member profiles the way
// CircleDetailScreen does. A real polish item, not a correctness gap: the
// supervisor can still tell individual vs. group submissions apart and
// cross-reference an id against Circle Detail's member list if needed.
private fun submitterSummary(submitter: SubmitterRef): String = when (submitter) {
    is SubmitterRef.Individual -> "من: ${submitter.userId}"
    is SubmitterRef.OnBehalfOfGroup -> "من: ${submitter.submittedByUserId} (مجموعة ${submitter.groupId})"
}

private fun evidenceSummary(submission: Submission): String = when (val evidence = submission.evidence) {
    is SubmissionEvidence.TimedReflection ->
        "المدة: ${evidence.actualDurationSeconds / 60} دقيقة — ${evidence.reflectionText}"
    is SubmissionEvidence.Media ->
        "تسجيل صوتي — ${evidence.durationSeconds} ثانية"
    SubmissionEvidence.CheckIn -> "تم تسجيل الحضور"
}
