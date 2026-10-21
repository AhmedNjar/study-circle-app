package com.studycircle.android.ui.task

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.IslamicTag
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.formatClock
import com.studycircle.android.ui.components.initialsOf
import com.studycircle.android.ui.components.toArabicDigits
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.Submission
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.model.SubmitterRef
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.ListSubmissionsUseCase
import com.studycircle.shared.features.task.domain.usecase.ReviewSubmissionUseCase
import com.studycircle.shared.features.task.presentation.review.ReviewQueueIntent
import com.studycircle.shared.features.task.presentation.review.ReviewQueueState
import com.studycircle.shared.features.task.presentation.review.ReviewQueueViewModel
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ReviewQueueScreen(taskId: String) {
    val listUseCase: ListSubmissionsUseCase = koinInject()
    val reviewUseCase: ReviewSubmissionUseCase = koinInject()
    val getTaskUseCase: GetTaskUseCase = koinInject()
    val getCircleUseCase: GetCircleUseCase = koinInject()
    val viewModel = rememberMviViewModel(key = taskId) { scope ->
        ReviewQueueViewModel(listUseCase, reviewUseCase, getTaskUseCase, getCircleUseCase, scope)
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(taskId) { viewModel.dispatch(ReviewQueueIntent.Load(taskId)) }

    StudyCircleTheme {
        ReviewQueueContent(
            state = state,
            onIntent = { viewModel.dispatch(it) }
        )
    }
}

@Composable
fun ReviewQueueContent(
    state: ReviewQueueState,
    onIntent: (ReviewQueueIntent) -> Unit
) {
    Scaffold(
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
                    Column {
                        Text(
                            "مراجعة المهام",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        state.task?.let {
                            Text(
                                it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                if (state.isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (state.submissions.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا توجد مهام بانتظار المراجعة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.submissions, key = { it.id }) { submission ->
                            ReviewCard(
                                submission = submission,
                                submitterName = submitterName(submission.submitter, state.memberNames),
                                task = state.task,
                                onApprove = { onIntent(ReviewQueueIntent.Approve(submission.id)) },
                                onReject = { onIntent(ReviewQueueIntent.Reject(submission.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    submission: Submission,
    submitterName: String,
    task: CircleTask?,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    PaperCard(modifier = Modifier.fillMaxWidth()) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initialsOf(submitterName),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = submitterName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "طلب مراجعة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    IslamicTag(text = "قيد الانتظار")
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = evidenceSummary(submission, task),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onReject,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF991B1B))
                    ) {
                        Text("رفض", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.size(8.dp))
                    TextButton(
                        onClick = onApprove,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text("قبول", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewQueueContentPreview() {
    StudyCircleTheme {
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

// Real names come from the circle's member list; an id we can't resolve (left the
// circle, offline) degrades to a neutral label rather than showing a raw id.
private fun submitterName(submitter: SubmitterRef, names: Map<String, String>): String = when (submitter) {
    is SubmitterRef.Individual -> names[submitter.userId] ?: "طالب"
    is SubmitterRef.OnBehalfOfGroup ->
        "مجموعة — بإرسال ${names[submitter.submittedByUserId] ?: "أحد الأعضاء"}"
}

private fun evidenceSummary(submission: Submission, task: CircleTask?): String = when (val evidence = submission.evidence) {
    is SubmissionEvidence.TimedReflection ->
        "المدة: ${(evidence.actualDurationSeconds / 60).toArabicDigits()} دقيقة — ${evidence.reflectionText}"
    is SubmissionEvidence.Media ->
        "تسجيل صوتي — ${formatClock(evidence.durationSeconds.toLong())}"
    is SubmissionEvidence.Count -> {
        val counting = task?.tracking as? TrackingMethod.CountBased
        if (counting != null) {
            "أنجز ${evidence.countAchieved.toArabicDigits()} من ${counting.target.toArabicDigits()} ${counting.unit}"
        } else {
            "أنجز ${evidence.countAchieved.toArabicDigits()}"
        }
    }
    SubmissionEvidence.CheckIn -> "أكّد الطالب إتمام المهمة"
}
