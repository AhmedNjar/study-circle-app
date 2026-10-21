package com.studycircle.shared.features.task.presentation.createtask

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.task.domain.model.ActivityType

// How completion is evidenced — independent of the activity type (a workout can
// be a rep count, a timed session, or a simple check-in). Mirrors TrackingMethod.
enum class TrackingChoice { TIMED_WITH_REFLECTION, MEDIA_SUBMISSION, COUNT_BASED, MANUAL_CHECK_IN }

// Individual-member targeting works end to end in the domain/data/backend
// layers (TaskAudience.IndividualMembers) but still has no picker here —
// same Phase 2 UI-scope note as before, just narrowed further now that
// Groups is the other real choice.
enum class AudienceChoice { ALL_MEMBERS, GROUPS }

sealed interface CreateTaskIntent : MviIntent {
    data class TitleChanged(val value: String) : CreateTaskIntent
    data class DueDateChanged(val value: String) : CreateTaskIntent
    data class ActivityTypeChanged(val value: ActivityType) : CreateTaskIntent
    data class TrackingChoiceChanged(val value: TrackingChoice) : CreateTaskIntent
    data class MinDurationMinutesChanged(val value: String) : CreateTaskIntent
    data class ReflectionMinLengthChanged(val value: String) : CreateTaskIntent
    data class CountUnitChanged(val value: String) : CreateTaskIntent
    data class CountTargetChanged(val value: String) : CreateTaskIntent
    data class CustomActivityLabelChanged(val value: String) : CreateTaskIntent
    data class AudienceChoiceChanged(val value: AudienceChoice) : CreateTaskIntent
    data object Submit : CreateTaskIntent
}

data class CreateTaskState(
    val title: String = "",
    val dueDate: String = "", // "YYYY-MM-DD"
    val activityType: ActivityType = ActivityType.READING,
    val trackingChoice: TrackingChoice = TrackingChoice.TIMED_WITH_REFLECTION,
    val minDurationMinutes: String = "15",
    val reflectionMinLength: String = "40",
    val countUnit: String = "صفحة",
    val countTarget: String = "10",
    // Only used when activityType == CUSTOM — becomes metadata.customFields["activityLabel"].
    val customActivityLabel: String = "",
    val audienceChoice: AudienceChoice = AudienceChoice.ALL_MEMBERS,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface CreateTaskEffect : MviEffect {
    data class Created(val taskId: String) : CreateTaskEffect
}
