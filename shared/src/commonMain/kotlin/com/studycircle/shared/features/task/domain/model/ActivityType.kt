package com.studycircle.shared.features.task.domain.model

// Known activities get real enum safety. A genuinely new one starts as CUSTOM
// with a free-text label in TaskMetadata.customFields — no domain change
// needed until it's common enough to earn its own enum value.
enum class ActivityType { READING, RECITATION, WATCHING, WORKOUT, CUSTOM }
