package com.studycircle.android.ui.task

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.studycircle.android.recording.AudioRecorderController
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.NavTab
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.android.ui.components.StudyCircleBottomBar
import com.studycircle.android.ui.components.activityEmoji
import com.studycircle.android.ui.components.activityLabel
import com.studycircle.android.ui.components.formatClock
import com.studycircle.android.ui.components.formatMinutes
import com.studycircle.android.ui.components.metadataSummary
import com.studycircle.android.ui.components.toArabicDigits
import com.studycircle.android.ui.components.toArabicLong
import com.studycircle.android.ui.components.trackingSummary
import com.studycircle.android.ui.mvi.rememberMviViewModel
import com.studycircle.android.ui.streak.StreakBadge
import com.studycircle.android.ui.theme.StudyCircleTheme
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
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerState
import com.studycircle.shared.features.task.presentation.taskrunner.TaskRunnerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

private val Emerald = Color(0xFF0A4232)
private val EmeraldLight = Color(0xFF0D5840)

// One screen for every kind of task. What it renders is decided by the task's
// DATA — tracking method (timer + reflection / recorder / counter / check-in)
// and audience (a group step first, when needed) — never by the task's id or
// title. That's what lets a workout, a lecture and a Quran recitation share it.
@Composable
fun TaskRunnerScreen(
    taskId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {}
) {
    val getTaskUseCase: GetTaskUseCase = koinInject()
    val submitEvidenceUseCase: SubmitEvidenceUseCase = koinInject()
    val listTaskGroupsUseCase: ListTaskGroupsUseCase = koinInject()
    val formOrJoinTaskGroupUseCase: FormOrJoinTaskGroupUseCase = koinInject()
    val authRepository: AuthRepository = koinInject()
    val viewModel = rememberMviViewModel(key = taskId) { scope ->
        TaskRunnerViewModel(
            getTaskUseCase, submitEvidenceUseCase, listTaskGroupsUseCase,
            formOrJoinTaskGroupUseCase, authRepository, scope
        )
    }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val recorder = remember { AudioRecorderController(context) }
    var recorderError by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    // Release the microphone if the user leaves mid-recording.
    DisposableEffect(recorder) { onDispose { recorder.cancel() } }

    LaunchedEffect(taskId) { viewModel.dispatch(TaskRunnerIntent.Load(taskId)) }

    // The ViewModel owns *whether* the timer runs; this owns the wall clock. Elapsed
    // time is derived from SystemClock (not by counting delay() iterations) so it
    // stays accurate if a tick is late. Restarting from state.elapsedSeconds also
    // makes pause/resume and rotation seamless.
    LaunchedEffect(state.isTimerRunning) {
        if (state.isTimerRunning) {
            val base = state.elapsedSeconds
            val startedAt = SystemClock.elapsedRealtime()
            while (true) {
                delay(500)
                viewModel.dispatch(TaskRunnerIntent.TimerTicked(base + (SystemClock.elapsedRealtime() - startedAt) / 1000))
            }
        }
    }

    fun startRecorder() {
        try {
            recorder.start()
            recorderError = null
        } catch (e: Exception) {
            recorder.cancel()
            recorderError = "تعذر بدء التسجيل على هذا الجهاز"
            viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startRecorder()
        } else {
            recorderError = "يلزم السماح باستخدام الميكروفون لتسجيل التلاوة"
            viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                TaskRunnerEffect.Submitted -> submitted = true
                TaskRunnerEffect.StartPlatformRecording -> {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) startRecorder() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                else -> {}
            }
        }
    }

    // Let the "sent" confirmation breathe for a moment before returning.
    LaunchedEffect(submitted) {
        if (submitted) {
            delay(1400)
            onSubmitted()
        }
    }

    StudyCircleTheme {
        TaskRunnerContent(
            state = state,
            submitted = submitted,
            recorderError = recorderError,
            onIntent = viewModel::dispatch,
            onStopRecording = {
                try {
                    val (path, seconds) = recorder.stop()
                    viewModel.dispatch(TaskRunnerIntent.RecordingFinished(path, seconds))
                } catch (e: Exception) {
                    recorder.cancel()
                    recorderError = "تعذر حفظ التسجيل، حاول مرة أخرى"
                    viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
                }
            },
            onDiscardRecording = {
                recorder.cancel()
                viewModel.dispatch(TaskRunnerIntent.DiscardRecording)
            },
            onBack = onBack,
            onNavigateTab = onNavigateTab
        )
    }
}

@Composable
fun TaskRunnerContent(
    state: TaskRunnerState,
    submitted: Boolean = false,
    recorderError: String? = null,
    onIntent: (TaskRunnerIntent) -> Unit = {},
    onStopRecording: () -> Unit = {},
    onDiscardRecording: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {}
) {
    val task = state.task

    Box(modifier = Modifier.fillMaxSize()) {
        IslamicBackground(Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                StudyCircleBottomBar(
                    currentTab = NavTab.HOME,
                    onTabSelected = { tab -> onNavigateTab(tab.id) }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding()
            ) {
                RunnerHeader(task = task, onBack = onBack)

                when {
                    state.isLoading && task == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    task == null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            state.errorMessage ?: "تعذر تحميل المهمة",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                    submitted -> SubmittedCard()
                    else -> RunnerBody(
                        task = task,
                        state = state,
                        recorderError = recorderError,
                        onIntent = onIntent,
                        onStopRecording = onStopRecording,
                        onDiscardRecording = onDiscardRecording
                    )
                }
            }
        }
    }
}

@Composable
private fun RunnerHeader(task: CircleTask?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(colors = listOf(Emerald, EmeraldLight)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp))
                Text(
                    "المحفظ والمتعلم",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawLine(Color.White, Offset(size.width * 0.35f, size.height * 0.15f), Offset(size.width * 0.7f, size.height * 0.5f), 2.5.dp.toPx(), StrokeCap.Round)
                        drawLine(Color.White, Offset(size.width * 0.7f, size.height * 0.5f), Offset(size.width * 0.35f, size.height * 0.85f), 2.5.dp.toPx(), StrokeCap.Round)
                    }
                }
            }

            if (task != null) {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    metadataSummary(task)?.let { details ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunnerBody(
    task: CircleTask,
    state: TaskRunnerState,
    recorderError: String?,
    onIntent: (TaskRunnerIntent) -> Unit,
    onStopRecording: () -> Unit,
    onDiscardRecording: () -> Unit
) {
    val needsGroup = task.audience is TaskAudience.Groups && state.myGroup == null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TaskInfoCard(task = task, groupId = state.myGroup?.id) }

        if (needsGroup) {
            item {
                GroupJoinCard(
                    groups = state.availableGroups,
                    newGroupName = state.newGroupName,
                    onNameChange = { onIntent(TaskRunnerIntent.NewGroupNameChanged(it)) },
                    onCreate = { onIntent(TaskRunnerIntent.CreateGroup) },
                    onJoin = { onIntent(TaskRunnerIntent.JoinGroup(it)) }
                )
            }
        } else {
            state.myGroup?.let { group ->
                item { MyGroupCard(group) }
            }
            item {
                PaperCard(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(16.dp)) {
                        when (val tracking = task.tracking) {
                            is TrackingMethod.TimedWithReflection -> TimedReflectionSection(
                                tracking = tracking,
                                state = state,
                                onIntent = onIntent
                            )
                            is TrackingMethod.MediaSubmission -> MediaSection(
                                recordingState = state.recordingState,
                                minDurationSeconds = tracking.minDurationSeconds,
                                onStart = { onIntent(TaskRunnerIntent.StartRecording) },
                                onStop = onStopRecording,
                                onDiscard = onDiscardRecording
                            )
                            is TrackingMethod.CountBased -> CountSection(
                                tracking = tracking,
                                count = state.countAchieved,
                                onChange = { onIntent(TaskRunnerIntent.CountChanged(it)) }
                            )
                            TrackingMethod.ManualCheckIn -> CheckInSection(task.activityType)
                        }
                    }
                }
            }

            (recorderError ?: state.errorMessage)?.let { message ->
                item {
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                if (state.isSubmitting) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    PrimaryButton(
                        text = if (task.tracking is TrackingMethod.ManualCheckIn) "تأكيد الإنجاز" else "إرسال للمراجعة",
                        onClick = { onIntent(TaskRunnerIntent.Submit) }
                    )
                }
            }
        }

        if (needsGroup) {
            state.errorMessage?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun TaskInfoCard(task: CircleTask, groupId: String?) {
    PaperCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(text = activityEmoji(task.activityType), fontSize = 22.sp) }

                // الرِّباط for this circle — the group's collective one for a group task
                // once the student is in a group, otherwise their own.
                StreakBadge(circleId = task.circleId, groupId = groupId)
            }

            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoCell("تاريخ الاستحقاق", task.dueDate.toArabicLong())
                InfoCell("نوع المهمة", activityLabel(task))
                InfoCell("المطلوب", trackingSummary(task.tracking))
            }
        }
    }
}

@Composable
private fun InfoCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MyGroupCard(group: TaskGroup) {
    PaperCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مجموعتك", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(
                "${group.name ?: "بدون اسم"} · ${group.memberIds.size.toArabicDigits()} أعضاء",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GroupJoinCard(
    groups: List<TaskGroup>,
    newGroupName: String,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit
) {
    PaperCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "هذه مهمة جماعية — انضم إلى مجموعة أو أنشئ مجموعتك",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            groups.forEach { group ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${group.name ?: "مجموعة بدون اسم"} (${group.memberIds.size.toArabicDigits()})",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { onJoin(group.id) }) { Text("انضمام", fontWeight = FontWeight.Bold) }
                }
            }
            if (groups.isNotEmpty()) Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newGroupName,
                onValueChange = onNameChange,
                label = { Text("اسم مجموعة جديدة") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(text = "إنشاء مجموعة", onClick = onCreate)
        }
    }
}

@Composable
private fun TimedReflectionSection(
    tracking: TrackingMethod.TimedWithReflection,
    state: TaskRunnerState,
    onIntent: (TaskRunnerIntent) -> Unit
) {
    val required = tracking.minDurationSeconds.toLong()
    val progress = if (required > 0) (state.elapsedSeconds.toFloat() / required).coerceIn(0f, 1f) else 1f
    val reached = state.elapsedSeconds >= required

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = formatClock(state.elapsedSeconds),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = if (reached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "الحد الأدنى: ${formatMinutes(tracking.minDurationSeconds / 60)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))
        ProgressBar(progress)
        Spacer(Modifier.height(16.dp))
        if (state.isTimerRunning) {
            OutlinedButton(onClick = { onIntent(TaskRunnerIntent.StopTimer) }, modifier = Modifier.fillMaxWidth()) {
                Text("إيقاف مؤقت", fontWeight = FontWeight.Bold)
            }
        } else {
            PrimaryButton(
                text = if (state.elapsedSeconds > 0) "متابعة" else "ابدأ الآن",
                onClick = { onIntent(TaskRunnerIntent.StartTimer) }
            )
        }
    }

    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
        value = state.reflectionText,
        onValueChange = { onIntent(TaskRunnerIntent.ReflectionChanged(it)) },
        label = { Text("ملخص أو تأمل") },
        supportingText = {
            Text("${state.reflectionText.length.toArabicDigits()} / ${tracking.reflectionMinLength.toArabicDigits()} حرفًا على الأقل")
        },
        minLines = 4,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun MediaSection(
    recordingState: RecordingState,
    minDurationSeconds: Int?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    // Live recording length for display only; the authoritative duration comes from
    // the recorder when it stops (RecordingFinished).
    var liveSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.Recording) {
            val startedAt = SystemClock.elapsedRealtime()
            while (true) {
                liveSeconds = (SystemClock.elapsedRealtime() - startedAt) / 1000
                delay(500)
            }
        } else {
            liveSeconds = 0
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        when (recordingState) {
            RecordingState.Idle -> {
                Text(
                    "اختر مكانًا هادئًا، ثم اضغط للبدء والتسميع بصوت واضح.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                minDurationSeconds?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "الحد الأدنى للتسجيل: ${formatMinutes(((it + 59) / 60))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "🎙  بدء التسجيل", onClick = onStart)
            }
            RecordingState.Recording -> {
                Text(
                    "● " + formatClock(liveSeconds),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
                Text("جارٍ التسجيل…", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "إيقاف التسجيل", onClick = onStop)
            }
            is RecordingState.Recorded -> {
                Text(
                    "تم التسجيل ✓",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "المدة: ${formatClock(recordingState.durationSeconds.toLong())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDiscard) { Text("إعادة التسجيل", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun CountSection(
    tracking: TrackingMethod.CountBased,
    count: Int,
    onChange: (Int) -> Unit
) {
    val progress = if (tracking.target > 0) (count.toFloat() / tracking.target).coerceIn(0f, 1f) else 1f
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "الهدف: ${tracking.target.toArabicDigits()} ${tracking.unit}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            StepperButton("−", enabled = count > 0) { onChange(count - 1) }
            Text(
                count.toArabicDigits(),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = if (count >= tracking.target) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
            )
            StepperButton("+", enabled = true) { onChange(count + 1) }
        }
        Text(tracking.unit, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(12.dp))
        ProgressBar(progress)
        Spacer(Modifier.height(8.dp))
        // Quick bump for big counts, so reaching e.g. 50 pages isn't 50 taps.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onChange(count + 5) }) { Text("+٥") }
            TextButton(onClick = { onChange(count + 10) }) { Text("+١٠") }
            TextButton(onClick = { onChange(tracking.target) }) { Text("إكمال الهدف") }
        }
    }
}

@Composable
private fun StepperButton(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) { Text(glyph, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun CheckInSection(type: ActivityType) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(activityEmoji(type), fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "هل أتممت هذه المهمة؟ اضغط «تأكيد الإنجاز» ليراجعها المشرف.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.secondary)
        )
    }
}

@Composable
private fun SubmittedCard() {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            PaperCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✓", fontSize = 44.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("تم الإرسال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "بارك الله فيك — بانتظار مراجعة المشرف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskRunnerCountPreview() {
    StudyCircleTheme {
        TaskRunnerContent(
            state = TaskRunnerState(
                task = CircleTask(
                    id = "t1", circleId = "c1", title = "تمرين الصباح",
                    dueDate = LocalDate(2026, 10, 25), createdBy = "s1",
                    activityType = ActivityType.WORKOUT,
                    tracking = TrackingMethod.CountBased(unit = "تمرين ضغط", target = 30),
                    audience = TaskAudience.AllMembers
                ),
                countAchieved = 12
            )
        )
    }
}
