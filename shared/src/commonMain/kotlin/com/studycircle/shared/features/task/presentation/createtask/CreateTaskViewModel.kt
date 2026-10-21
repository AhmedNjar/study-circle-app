package com.studycircle.shared.features.task.presentation.createtask

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.NewTaskInput
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TaskMetadata
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.CreateTaskUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDate

// Audience choices are AllMembers or Groups; per-member targeting
// (TaskAudience.IndividualMembers) works end to end in the domain/data/backend
// layers but still has no picker — a UI scope line, not an architecture gap.
class CreateTaskViewModel(
    private val circleId: String,
    private val createTaskUseCase: CreateTaskUseCase,
    scope: CoroutineScope
) : MviViewModel<CreateTaskIntent, CreateTaskState, CreateTaskEffect>(CreateTaskState(), scope) {

    override suspend fun handleIntent(intent: CreateTaskIntent) {
        when (intent) {
            is CreateTaskIntent.TitleChanged -> setState { copy(title = intent.value, errorMessage = null) }
            is CreateTaskIntent.DueDateChanged -> setState { copy(dueDate = intent.value, errorMessage = null) }
            // Picking an activity pre-selects the evidence method that usually fits it;
            // the supervisor can still override it (a workout can be timed, counted
            // or just checked in).
            is CreateTaskIntent.ActivityTypeChanged -> setState {
                copy(
                    activityType = intent.value,
                    trackingChoice = defaultTrackingFor(intent.value),
                    errorMessage = null
                )
            }
            is CreateTaskIntent.TrackingChoiceChanged -> setState { copy(trackingChoice = intent.value) }
            is CreateTaskIntent.MinDurationMinutesChanged -> setState { copy(minDurationMinutes = intent.value) }
            is CreateTaskIntent.ReflectionMinLengthChanged -> setState { copy(reflectionMinLength = intent.value) }
            is CreateTaskIntent.CountUnitChanged -> setState { copy(countUnit = intent.value, errorMessage = null) }
            is CreateTaskIntent.CountTargetChanged -> setState { copy(countTarget = intent.value, errorMessage = null) }
            is CreateTaskIntent.CustomActivityLabelChanged -> setState { copy(customActivityLabel = intent.value, errorMessage = null) }
            is CreateTaskIntent.AudienceChoiceChanged -> setState { copy(audienceChoice = intent.value) }
            CreateTaskIntent.Submit -> submit()
        }
    }

    private suspend fun submit() {
        val s = currentState
        if (s.title.isBlank() || s.dueDate.isBlank()) {
            setState { copy(errorMessage = "أدخل العنوان والتاريخ") }
            return
        }
        val dueDate = runCatching { LocalDate.parse(s.dueDate) }.getOrNull()
        if (dueDate == null) {
            setState { copy(errorMessage = "صيغة التاريخ غير صحيحة (YYYY-MM-DD)") }
            return
        }

        val tracking: TrackingMethod = when (s.trackingChoice) {
            TrackingChoice.TIMED_WITH_REFLECTION -> {
                val minutes = s.minDurationMinutes.toIntOrNull()
                val reflectionLen = s.reflectionMinLength.toIntOrNull()
                if (minutes == null || reflectionLen == null) {
                    setState { copy(errorMessage = "أدخل قيمًا صحيحة") }
                    return
                }
                TrackingMethod.TimedWithReflection(minDurationSeconds = minutes * 60, reflectionMinLength = reflectionLen)
            }
            TrackingChoice.MEDIA_SUBMISSION -> TrackingMethod.MediaSubmission(allowedTypes = setOf(MediaType.AUDIO))
            TrackingChoice.COUNT_BASED -> {
                val target = s.countTarget.toIntOrNull()
                if (target == null || target <= 0 || s.countUnit.isBlank()) {
                    setState { copy(errorMessage = "أدخل الوحدة وهدفًا رقميًا أكبر من صفر") }
                    return
                }
                TrackingMethod.CountBased(unit = s.countUnit.trim(), target = target)
            }
            TrackingChoice.MANUAL_CHECK_IN -> TrackingMethod.ManualCheckIn
        }

        // CUSTOM has no enum value of its own to say what the activity IS, so the
        // supervisor's free-text label travels in metadata (see API contract).
        val metadata = if (s.activityType == ActivityType.CUSTOM) {
            if (s.customActivityLabel.isBlank()) {
                setState { copy(errorMessage = "اكتب اسم النشاط المخصص") }
                return
            }
            TaskMetadata(customFields = mapOf("activityLabel" to s.customActivityLabel.trim()))
        } else {
            TaskMetadata()
        }

        setState { copy(isLoading = true, errorMessage = null) }
        val audience = when (s.audienceChoice) {
            AudienceChoice.ALL_MEMBERS -> TaskAudience.AllMembers
            // groupIds starts empty — groups form after the task exists, via
            // TaskGroupController, not at creation time. See TaskAudienceDto.Groups.
            AudienceChoice.GROUPS -> TaskAudience.Groups(groupIds = emptyList())
        }
        val input = NewTaskInput(
            title = s.title, dueDate = dueDate, activityType = s.activityType,
            tracking = tracking, audience = audience, metadata = metadata
        )
        createTaskUseCase(circleId, input).fold(
            onSuccess = { task ->
                setState { copy(isLoading = false) }
                emitEffect(CreateTaskEffect.Created(task.id))
            },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر إنشاء المهمة") } }
        )
    }

    private fun defaultTrackingFor(type: ActivityType): TrackingChoice = when (type) {
        ActivityType.READING, ActivityType.WATCHING -> TrackingChoice.TIMED_WITH_REFLECTION
        ActivityType.RECITATION -> TrackingChoice.MEDIA_SUBMISSION
        ActivityType.WORKOUT -> TrackingChoice.COUNT_BASED
        ActivityType.CUSTOM -> TrackingChoice.MANUAL_CHECK_IN
    }
}
