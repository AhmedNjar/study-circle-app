package com.studycircle.android.ui.task

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import com.studycircle.android.ui.streak.StreakBadge
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TaskGroup
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.FormOrJoinTaskGroupUseCase
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.ListTaskGroupsUseCase
import com.studycircle.shared.features.task.domain.usecase.SubmitEvidenceUseCase
import com.studycircle.shared.features.task.presentation.taskrunner.RecordingState
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerEffect
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerIntent
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerViewModel
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate

// The data-driven screen from the UI/UX discussion: one screen, rendered
// differently depending on task.tracking — a timer+reflection form or an
// audio recorder. For a Groups-audience task, it now shows a join/create
// step first (see GroupJoinRunner below) until the student has a group;
// after that it's the same timer/recorder UI as an individual task.
@Composable
fun TaskRunnerScreen(taskId: String, onSubmitted: () -> Unit) {
    val getTaskUseCase: GetTaskUseCase = koinInject()
    val submitEvidenceUseCase: SubmitEvidenceUseCase = koinInject()
    val listTaskGroupsUseCase: ListTaskGroupsUseCase = koinInject()
    val formOrJoinTaskGroupUseCase: FormOrJoinTaskGroupUseCase = koinInject()
    val authRepository: AuthRepository = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember {
        TaskRunnerViewModel(
            getTaskUseCase, submitEvidenceUseCase, listTaskGroupsUseCase,
            formOrJoinTaskGroupUseCase, authRepository, scope
        )
    }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val recorder = remember { AudioRecorderController(context) }

    LaunchedEffect(taskId) { viewModel.dispatch(TaskRunnerIntent.Load(taskId)) }

    // The ViewModel owns *whether* the timer is running; this owns the
    // actual wall-clock loop, reporting back via TimerTicked.
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
        onIntent = viewModel::dispatch,
        onStopRecording = {
            val (path, duration) = recorder.stop()
            viewModel.dispatch(TaskRunnerIntent.RecordingFinished(path, duration))
        },
        onDiscardRecording = {
            recorder.cancel()
            viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
        }
    )
}

@Composable
fun TaskRunnerContent(
    state: TaskRunnerState,
    onIntent: (TaskRunnerIntent) -> Unit,
    onStopRecording: () -> Unit = {},
    onDiscardRecording: () -> Unit = {}
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

                val needsGroup = task.audience is TaskAudience.Groups && state.myGroup == null
                if (needsGroup) {
                    GroupJoinRunner(
                        availableGroups = state.availableGroups,
                        newGroupName = state.newGroupName,
                        onNameChange = { onIntent(TaskRunnerIntent.NewGroupNameChanged(it)) },
                        onCreate = { onIntent(TaskRunnerIntent.CreateGroup) },
                        onJoin = { groupId -> onIntent(TaskRunnerIntent.JoinGroup(groupId)) }
                    )
                } else {
                    state.myGroup?.let { group ->
                        Text(
                            "مجموعتك: ${group.name ?: "بدون اسم"} (${group.memberIds.size} أعضاء)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        StreakBadge(circleId = task.circleId, groupId = group.id)
                        Spacer(Modifier.height(16.dp))
                    }

                    when (val tracking = task.tracking) {
                        is TrackingMethod.TimedWithReflection -> TimedReflectionRunner(
                            minDurationSeconds = tracking.minDurationSeconds,
                            elapsedSeconds = state.elapsedSeconds,
                            isRunning = state.isTimerRunning,
                            reflectionText = state.reflectionText,
                            onStartStop = {
                                onIntent(if (state.isTimerRunning) TaskRunnerIntent.StopTimer else TaskRunnerIntent.StartTimer)
                            },
                            onReflectionChange = { onIntent(TaskRunnerIntent.ReflectionChanged(it)) }
                        )
                        is TrackingMethod.MediaSubmission -> MediaRunner(
                            recordingState = state.recordingState,
                            onStart = { onIntent(TaskRunnerIntent.StartRecording) },
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
                    else PrimaryButton(text = "إرسال", onClick = { onIntent(TaskRunnerIntent.Submit) })
                }

                if (needsGroup) {
                    state.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskRunnerContentPreview() {
    val mockTask = CircleTask(
        id = "1",
        circleId = "c1",
        title = "قراءة ورد اليوم",
        dueDate = LocalDate(2023, 10, 10),
        createdBy = "u1",
        activityType = ActivityType.READING,
        tracking = TrackingMethod.TimedWithReflection(600, 100),
        audience = TaskAudience.AllMembers
    )
    MaterialTheme {
        TaskRunnerContent(
            state = TaskRunnerState(task = mockTask),
            onIntent = {}
        )
    }
}

@Composable
private fun GroupJoinRunner(
    availableGroups: List<TaskGroup>,
    newGroupName: String,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit
) {
    Text("انضم إلى مجموعة أو أنشئ واحدة جديدة", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    if (availableGroups.isNotEmpty()) {
        availableGroups.forEach { group ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(group.name ?: "مجموعة بدون اسم", modifier = Modifier)
                    TextButton(onClick = { onJoin(group.id) }) { Text("انضمام") }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    OutlinedTextField(
        value = newGroupName,
        onValueChange = onNameChange,
        label = { Text("اسم مجموعة جديدة") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    PrimaryButton(text = "إنشاء مجموعة", onClick = onCreate)
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
