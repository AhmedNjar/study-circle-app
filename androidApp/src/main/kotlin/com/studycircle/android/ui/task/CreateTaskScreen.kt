package com.studycircle.android.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studycircle.android.ui.components.IslamicBackground
import com.studycircle.android.ui.components.PaperCard
import com.studycircle.android.ui.components.PrimaryButton
import com.studycircle.android.ui.components.activityEmoji
import com.studycircle.android.ui.mvi.rememberMviViewModel
import com.studycircle.android.ui.theme.StudyCircleTheme
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.usecase.CreateTaskUseCase
import com.studycircle.shared.features.task.presentation.createtask.AudienceChoice
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskEffect
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskIntent
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskState
import com.studycircle.shared.features.task.presentation.createtask.CreateTaskViewModel
import com.studycircle.shared.features.task.presentation.createtask.TrackingChoice
import com.studycircle.shared.localization.ArabicTerms
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

private val Emerald = Color(0xFF0A4232)
private val EmeraldLight = Color(0xFF0D5840)

// A flat, scrollable form. The VM contract doesn't care how many screens fill it
// in, so this can become a stepper later without touching CreateTaskViewModel.
@Composable
fun CreateTaskScreen(circleId: String, onCreated: (taskId: String) -> Unit) {
    val useCase: CreateTaskUseCase = koinInject()
    val viewModel = rememberMviViewModel(key = circleId) { scope -> CreateTaskViewModel(circleId, useCase, scope) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CreateTaskEffect.Created -> onCreated(effect.taskId)
            }
        }
    }

    StudyCircleTheme {
        CreateTaskContent(state = state, onIntent = viewModel::dispatch)
    }
}

private fun trackingLabel(choice: TrackingChoice): String = when (choice) {
    TrackingChoice.TIMED_WITH_REFLECTION -> "وقت وملخص"
    TrackingChoice.MEDIA_SUBMISSION -> "تسجيل صوتي"
    TrackingChoice.COUNT_BASED -> "عدّاد"
    TrackingChoice.MANUAL_CHECK_IN -> "تأكيد يدوي"
}

private fun trackingHint(choice: TrackingChoice): String = when (choice) {
    TrackingChoice.TIMED_WITH_REFLECTION -> "يبدأ الطالب مؤقتًا ثم يكتب ملخصًا — مناسب للقراءة والمشاهدة."
    TrackingChoice.MEDIA_SUBMISSION -> "يسجّل الطالب صوته ويراجعه المشرف — مناسب للتسميع."
    TrackingChoice.COUNT_BASED -> "يحدد الطالب ما أنجزه (صفحات، تمارين، أذكار…) مقابل هدف."
    TrackingChoice.MANUAL_CHECK_IN -> "ضغطة تأكيد واحدة يراجعها المشرف — لأي مهمة مخصّصة."
}

@Composable
fun CreateTaskContent(
    state: CreateTaskState,
    onIntent: (CreateTaskIntent) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        IslamicBackground(Modifier.fillMaxSize())

        Column(Modifier.fillMaxSize().imePadding()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Emerald, EmeraldLight)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Text(
                        "مهمة جديدة",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "حدّد النشاط وكيف يُثبت الطالب إنجازه",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PaperCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel("التفاصيل")
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { onIntent(CreateTaskIntent.TitleChanged(it)) },
                            label = { Text("عنوان المهمة") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors()
                        )
                        OutlinedTextField(
                            value = state.dueDate,
                            onValueChange = { onIntent(CreateTaskIntent.DueDateChanged(it)) },
                            label = { Text("تاريخ الاستحقاق (YYYY-MM-DD)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors()
                        )
                    }
                }

                PaperCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel("نوع النشاط")
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActivityType.entries.forEach { type ->
                                FilterChip(
                                    selected = state.activityType == type,
                                    onClick = { onIntent(CreateTaskIntent.ActivityTypeChanged(type)) },
                                    label = { Text("${activityEmoji(type)}  ${ArabicTerms.activityTypeLabel(type)}") }
                                )
                            }
                        }
                        if (state.activityType == ActivityType.CUSTOM) {
                            OutlinedTextField(
                                value = state.customActivityLabel,
                                onValueChange = { onIntent(CreateTaskIntent.CustomActivityLabelChanged(it)) },
                                label = { Text("اسم النشاط (مثال: حفظ متن، مراجعة درس)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = fieldColors()
                            )
                        }
                    }
                }

                PaperCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel("طريقة التتبع")
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrackingChoice.entries.forEach { choice ->
                                FilterChip(
                                    selected = state.trackingChoice == choice,
                                    onClick = { onIntent(CreateTaskIntent.TrackingChoiceChanged(choice)) },
                                    label = { Text(trackingLabel(choice)) }
                                )
                            }
                        }
                        Text(
                            trackingHint(state.trackingChoice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )

                        when (state.trackingChoice) {
                            TrackingChoice.TIMED_WITH_REFLECTION -> {
                                OutlinedTextField(
                                    value = state.minDurationMinutes,
                                    onValueChange = { onIntent(CreateTaskIntent.MinDurationMinutesChanged(it)) },
                                    label = { Text("الحد الأدنى بالدقائق") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = fieldColors()
                                )
                                OutlinedTextField(
                                    value = state.reflectionMinLength,
                                    onValueChange = { onIntent(CreateTaskIntent.ReflectionMinLengthChanged(it)) },
                                    label = { Text("الحد الأدنى لطول الملخص (بالحرف)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = fieldColors()
                                )
                            }
                            TrackingChoice.COUNT_BASED -> {
                                OutlinedTextField(
                                    value = state.countTarget,
                                    onValueChange = { onIntent(CreateTaskIntent.CountTargetChanged(it)) },
                                    label = { Text("الهدف (عدد)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = fieldColors()
                                )
                                OutlinedTextField(
                                    value = state.countUnit,
                                    onValueChange = { onIntent(CreateTaskIntent.CountUnitChanged(it)) },
                                    label = { Text("الوحدة (صفحة، تمرين، آية…)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = fieldColors()
                                )
                            }
                            TrackingChoice.MEDIA_SUBMISSION, TrackingChoice.MANUAL_CHECK_IN -> Unit
                        }
                    }
                }

                PaperCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel("المشاركون")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = state.audienceChoice == AudienceChoice.ALL_MEMBERS,
                                onClick = { onIntent(CreateTaskIntent.AudienceChoiceChanged(AudienceChoice.ALL_MEMBERS)) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("جميع الأعضاء") }
                            SegmentedButton(
                                selected = state.audienceChoice == AudienceChoice.GROUPS,
                                onClick = { onIntent(CreateTaskIntent.AudienceChoiceChanged(AudienceChoice.GROUPS)) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("مجموعات") }
                        }
                        if (state.audienceChoice == AudienceChoice.GROUPS) {
                            Text(
                                "ينضم الطلاب إلى مجموعات أو يكوّنونها بأنفسهم، وتُحتسب الرِّباط للمجموعة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    PrimaryButton(text = "إنشاء المهمة", onClick = { onIntent(CreateTaskIntent.Submit) })
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

@Preview(showBackground = true)
@Composable
fun CreateTaskContentPreview() {
    StudyCircleTheme {
        CreateTaskContent(
            state = CreateTaskState(activityType = ActivityType.WORKOUT, trackingChoice = TrackingChoice.COUNT_BASED),
            onIntent = {}
        )
    }
}
