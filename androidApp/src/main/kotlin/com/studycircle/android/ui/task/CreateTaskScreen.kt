package com.studycircle.android.ui.task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.usecase.CreateTaskUseCase
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskEffect
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskIntent
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskViewModel
import com.studycircle.shared.features.task.presentation.createtask.TrackingChoice
import com.studycircle.shared.localization.ArabicTerms
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

// A single-screen form for Phase 2, not the multi-step wizard sketched in
// the UI/UX discussion — a working flat form now, upgradeable to a stepper
// later without touching CreateTaskViewModel (the contract doesn't care how
// many screens fill it in).
@Composable
fun CreateTaskScreen(circleId: String, onCreated: (taskId: String) -> Unit) {
    val useCase: CreateTaskUseCase = koinInject()
    val scope = rememberCoroutineScope()
    val viewModel = remember { CreateTaskViewModel(circleId, useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CreateTaskEffect.Created -> onCreated(effect.taskId)
            }
        }
    }

    CreateTaskContent(
        state = state,
        onTitleChanged = { viewModel.dispatch(CreateTaskIntent.TitleChanged(it)) },
        onDueDateChanged = { viewModel.dispatch(CreateTaskIntent.DueDateChanged(it)) },
        onActivityTypeChanged = { viewModel.dispatch(CreateTaskIntent.ActivityTypeChanged(it)) },
        onTrackingChoiceChanged = { viewModel.dispatch(CreateTaskIntent.TrackingChoiceChanged(it)) },
        onMinDurationMinutesChanged = { viewModel.dispatch(CreateTaskIntent.MinDurationMinutesChanged(it)) },
        onReflectionMinLengthChanged = { viewModel.dispatch(CreateTaskIntent.ReflectionMinLengthChanged(it)) },
        onSubmit = { viewModel.dispatch(CreateTaskIntent.Submit) }
    )
}

@Composable
fun CreateTaskContent(
    state: com.studycircle.shared.features.task.presentation.createtask.CreateTaskState,
    onTitleChanged: (String) -> Unit,
    onDueDateChanged: (String) -> Unit,
    onActivityTypeChanged: (ActivityType) -> Unit,
    onTrackingChoiceChanged: (TrackingChoice) -> Unit,
    onMinDurationMinutesChanged: (String) -> Unit,
    onReflectionMinLengthChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("مهمة جديدة", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChanged,
            label = { Text("عنوان المهمة") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = state.dueDate,
            onValueChange = onDueDateChanged,
            label = { Text("تاريخ الاستحقاق (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text("نوع النشاط", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        val activityChoices = ActivityType.entries.filter { it != ActivityType.CUSTOM }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            activityChoices.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = state.activityType == type,
                    onClick = { onActivityTypeChanged(type) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = activityChoices.size)
                ) { Text(ArabicTerms.activityTypeLabel(type)) }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("طريقة التتبع", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.trackingChoice == TrackingChoice.TIMED_WITH_REFLECTION,
                onClick = { onTrackingChoiceChanged(TrackingChoice.TIMED_WITH_REFLECTION) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("قراءة بوقت وملخص") }
            SegmentedButton(
                selected = state.trackingChoice == TrackingChoice.MEDIA_SUBMISSION,
                onClick = { onTrackingChoiceChanged(TrackingChoice.MEDIA_SUBMISSION) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("تسجيل صوتي") }
        }

        Spacer(Modifier.height(12.dp))
        if (state.trackingChoice == TrackingChoice.TIMED_WITH_REFLECTION) {
            OutlinedTextField(
                value = state.minDurationMinutes,
                onValueChange = onMinDurationMinutesChanged,
                label = { Text("الحد الأدنى بالدقائق") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.reflectionMinLength,
                onValueChange = onReflectionMinLengthChanged,
                label = { Text("الحد الأدنى لطول الملخص (بالحرف)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        if (state.isLoading) CircularProgressIndicator()
        else PrimaryButton(text = "إنشاء المهمة", onClick = onSubmit)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CreateTaskPreview() {
    MaterialTheme {
        CreateTaskContent(
            state = com.studycircle.shared.features.task.presentation.createtask.CreateTaskState(
                title = "مهمة تجريبية",
                dueDate = "2023-12-31"
            ),
            onTitleChanged = {},
            onDueDateChanged = {},
            onActivityTypeChanged = {},
            onTrackingChoiceChanged = {},
            onMinDurationMinutesChanged = {},
            onReflectionMinLengthChanged = {},
            onSubmit = {}
        )
    }
}
