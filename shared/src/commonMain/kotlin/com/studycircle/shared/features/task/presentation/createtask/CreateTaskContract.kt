package com.studycircle.shared.features.task.presentation.createtask

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.task.domain.model.ActivityType

// Phase 2's create-task UI only offers the two tracking methods the original
// spec named concretely. CountBased/ManualCheckIn get their own choices here
// once Phase 5 adds watching/workout activities to the picker.
enum class TrackingChoice { TIMED_WITH_REFLECTION, MEDIA_SUBMISSION }

sealed interface CreateTaskIntent : MviIntent {
    data class TitleChanged(val value: String) : CreateTaskIntent
    data class DueDateChanged(val value: String) : CreateTaskIntent
    data class ActivityTypeChanged(val value: ActivityType) : CreateTaskIntent
    data class TrackingChoiceChanged(val value: TrackingChoice) : CreateTaskIntent
    data class MinDurationMinutesChanged(val value: String) : CreateTaskIntent
    data class ReflectionMinLengthChanged(val value: String) : CreateTaskIntent
    data object Submit : CreateTaskIntent
}

data class CreateTaskState(
    val title: String = "",
    val dueDate: String = "", // "YYYY-MM-DD"
    val activityType: ActivityType = ActivityType.READING,
    val trackingChoice: TrackingChoice = TrackingChoice.TIMED_WITH_REFLECTION,
    val minDurationMinutes: String = "15",
    val reflectionMinLength: String = "40",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface CreateTaskEffect : MviEffect {
    data class Created(val taskId: String) : CreateTaskEffect
}
