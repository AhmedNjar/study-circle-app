package com.studycircle.shared.features.task.domain.model

import kotlinx.datetime.LocalDate

data class CircleTask(
    val id: String,
    val circleId: String,
    val title: String,
    val dueDate: LocalDate,
    val createdBy: String,   // supervisorId
    val activityType: ActivityType,
    val tracking: TrackingMethod,
    val audience: TaskAudience,
    val metadata: TaskMetadata = TaskMetadata()
)

// Activity-specific optional details. Deliberately a bag, not more sealed
// subclasses — the field that absorbs new activity types without touching
// CircleTask itself.
data class TaskMetadata(
    val bookTitle: String? = null,
    val pageRange: IntRange? = null,
    val surah: String? = null,
    val ayahRange: IntRange? = null,
    val customFields: Map<String, String> = emptyMap()
)
