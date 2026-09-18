package com.studycircle.android.ui.task

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.studycircle.android.recording.AudioRecorderController
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.SubmitEvidenceUseCase
import com.studycircle.shared.features.task.presentation.taskrunner.RecordingState
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerEffect
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerIntent
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

@Composable
fun TaskRunnerScreen(taskId: String, onSubmitted: () -> Unit) {
    val getTaskUseCase: GetTaskUseCase = koinInject()
    val submitEvidenceUseCase: SubmitEvidenceUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { TaskRunnerViewModel(getTaskUseCase, submitEvidenceUseCase, scope) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val recorder = remember { AudioRecorderController(context) }

    LaunchedEffect(taskId) { viewModel.dispatch(TaskRunnerIntent.Load(taskId)) }

    LaunchedEffect(state.isTimerRunning) {
        var elapsed = state.elapsedSeconds
        while (state.isTimerRunning) {
            delay(1000)
            elapsed += 1
            viewModel.dispatch(TaskRunnerIntent.TimerTicked(elapsed))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) recorder.start() else viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                TaskRunnerEffect.Submitted -> onSubmitted()
                TaskRunnerEffect.StartPlatformRecording -> {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) recorder.start() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                else -> {}
            }
        }
    }

    TaskRunnerContent(
        state = state,
        onStartStopTimer = {
            viewModel.dispatch(if (state.isTimerRunning) TaskRunnerIntent.StopTimer else TaskRunnerIntent.StartTimer)
        },
        onReflectionChange = { viewModel.dispatch(TaskRunnerIntent.ReflectionChanged(it)) },
        onStartRecording = { viewModel.dispatch(TaskRunnerIntent.StartRecording) },
        onStopRecording = {
            val (path, duration) = recorder.stop()
            viewModel.dispatch(TaskRunnerIntent.RecordingFinished(path, duration))
        },
        onDiscardRecording = {
            recorder.cancel()
            viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
        },
        onSubmit = { viewModel.dispatch(TaskRunnerIntent.Submit) }
    )
}

@Composable
fun TaskRunnerContent(
    state: com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerState,
    onStartStopTimer: () -> Unit,
    onReflectionChange: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDiscardRecording: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.task == null -> Text(state.errorMessage ?: "تعذر تحميل المهمة")
            else -> {
                val task = state.task!!
                Text(task.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                when (val tracking = task.tracking) {
                    is TrackingMethod.TimedWithReflection -> TimedReflectionRunner(
                        minDurationSeconds = tracking.minDurationSeconds,
                        elapsedSeconds = state.elapsedSeconds,
                        isRunning = state.isTimerRunning,
                        reflectionText = state.reflectionText,
                        onStartStop = onStartStopTimer,
                        onReflectionChange = onReflectionChange
                    )
                    is TrackingMethod.MediaSubmission -> MediaRunner(
                        recordingState = state.recordingState,
                        onStart = onStartRecording,
                        onStop = onStopRecording,
                        onDiscard = onDiscardRecording
                    )
                    else -> Text("نوع المهمة هذا غير مدعوم بعد")
                }

                state.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(16.dp))
                if (state.isSubmitting) CircularProgressIndicator()
                else PrimaryButton(text = "إرسال", onClick = onSubmit)
            }
        }
    }
}

@Composable
private fun TimedReflectionRunner(
    minDurationSeconds: Int,
    elapsedSeconds: Long,
    isRunning: Boolean,
    reflectionText: String,
    onStartStop: () -> Unit,
    onReflectionChange: (String) -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    Text("الوقت: $minutes:${seconds.toString().padStart(2, '0')}  (الحد الأدنى: ${minDurationSeconds / 60} دقيقة)")
    Spacer(Modifier.height(12.dp))
    PrimaryButton(text = if (isRunning) "إيقاف المؤقت" else "بدء القراءة", onClick = onStartStop)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = reflectionText,
        onValueChange = onReflectionChange,
        label = { Text("ملخص أو تأمل عن القراءة") },
        minLines = 4,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MediaRunner(
    recordingState: RecordingState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    when (recordingState) {
        RecordingState.Idle -> PrimaryButton(text = "بدء التسجيل", onClick = onStart)
        RecordingState.Recording -> PrimaryButton(text = "إيقاف التسجيل", onClick = onStop)
        is RecordingState.Recorded -> {
            Text("تم التسجيل — المدة: ${recordingState.durationSeconds} ثانية")
            Spacer(Modifier.height(8.dp))
            Row {
                TextButton(onClick = onDiscard) { Text("إعادة التسجيل") }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TaskRunnerPreview() {
    MaterialTheme {
        TaskRunnerContent(
            state = com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerState(
                task = CircleTask(
                    id = "1",
                    circleId = "c1",
                    title = "مهمة تجريبية",
                    dueDate = LocalDate(2023, 12, 31),
                    createdBy = "sup1",
                    activityType = ActivityType.READING,
                    tracking = TrackingMethod.TimedWithReflection(600, 100),
                    audience = TaskAudience.AllMembers
                ),
                elapsedSeconds = 300,
                isTimerRunning = true,
                reflectionText = "تأملات..."
            ),
            onStartStopTimer = {},
            onReflectionChange = {},
            onStartRecording = {},
            onStopRecording = {},
            onDiscardRecording = {},
            onSubmit = {}
        )
    }
}
