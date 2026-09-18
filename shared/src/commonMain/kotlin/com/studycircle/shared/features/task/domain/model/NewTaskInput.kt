package com.studycircle.shared.features.task.domain.model

import kotlinx.datetime.LocalDate

// What a supervisor submits to create a task — no id/createdBy, the server
// assigns those. Kept separate from CircleTask rather than making its fields
// nullable there.
data class NewTaskInput(
    val title: String,
    val dueDate: LocalDate,
    val activityType: ActivityType,
    val tracking: TrackingMethod,
    val audience: TaskAudience,
    val metadata: TaskMetadata = TaskMetadata()
)
