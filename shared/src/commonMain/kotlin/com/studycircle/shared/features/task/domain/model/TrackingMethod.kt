package com.studycircle.shared.features.task.domain.model

// What evidence a task requires — independent of ActivityType. e.g. READING
// can be TimedWithReflection *or* CountBased depending on the task.
sealed class TrackingMethod {
    data class TimedWithReflection(
        val minDurationSeconds: Int,   // anti-idle floor
        val reflectionMinLength: Int   // anti-idle-cheating guard
    ) : TrackingMethod()

    data class MediaSubmission(
        val allowedTypes: Set<MediaType>,
        val minDurationSeconds: Int? = null
    ) : TrackingMethod()

    data class CountBased(
        val unit: String,   // free label: "pages", "reps", "km", "ayat"
        val target: Int
    ) : TrackingMethod()

    data object ManualCheckIn : TrackingMethod()
}

enum class MediaType { AUDIO, VIDEO }
