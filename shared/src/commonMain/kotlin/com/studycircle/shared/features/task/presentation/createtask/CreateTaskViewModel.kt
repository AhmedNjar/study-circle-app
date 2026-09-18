package com.studycircle.shared.features.task.presentation.createtask

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.NewTaskInput
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.features.task.domain.usecase.CreateTaskUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDate

// Audience is fixed to AllMembers here — Phase 2's UI doesn't expose
// per-member or group targeting yet, even though the domain/data/backend
// layers already support IndividualMembers. That's a UI scope line, not an
// architecture gap: TaskAudience.IndividualMembers works end to end today,
// there's just no picker for it.
class CreateTaskViewModel(
    private val circleId: String,
    private val createTaskUseCase: CreateTaskUseCase,
    scope: CoroutineScope
) : MviViewModel<CreateTaskIntent, CreateTaskState, CreateTaskEffect>(CreateTaskState(), scope) {

    override suspend fun handleIntent(intent: CreateTaskIntent) {
        when (intent) {
            is CreateTaskIntent.TitleChanged -> setState { copy(title = intent.value, errorMessage = null) }
            is CreateTaskIntent.DueDateChanged -> setState { copy(dueDate = intent.value, errorMessage = null) }
            is CreateTaskIntent.ActivityTypeChanged -> setState { copy(activityType = intent.value) }
            is CreateTaskIntent.TrackingChoiceChanged -> setState { copy(trackingChoice = intent.value) }
            is CreateTaskIntent.MinDurationMinutesChanged -> setState { copy(minDurationMinutes = intent.value) }
            is CreateTaskIntent.ReflectionMinLengthChanged -> setState { copy(reflectionMinLength = intent.value) }
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
        }

        setState { copy(isLoading = true, errorMessage = null) }
        val input = NewTaskInput(
            title = s.title, dueDate = dueDate, activityType = s.activityType,
            tracking = tracking, audience = TaskAudience.AllMembers
        )
        createTaskUseCase(circleId, input).fold(
            onSuccess = { task ->
                setState { copy(isLoading = false) }
                emitEffect(CreateTaskEffect.Created(task.id))
            },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر إنشاء المهمة") } }
        )
    }
}
