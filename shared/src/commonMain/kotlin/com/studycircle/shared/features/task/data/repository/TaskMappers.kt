package com.studycircle.shared.features.task.data.repository

import com.studycircle.shared.features.task.data.remote.dto.CreateTaskRequestDto
import com.studycircle.shared.features.task.data.remote.dto.TaskAudienceDto
import com.studycircle.shared.features.task.data.remote.dto.TaskMetadataDto
import com.studycircle.shared.features.task.data.remote.dto.TaskResponseDto
import com.studycircle.shared.features.task.data.remote.dto.TrackingMethodDto
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.NewTaskInput
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.model.TaskMetadata
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import kotlinx.datetime.LocalDate

internal fun NewTaskInput.toDto() = CreateTaskRequestDto(
    title = title,
    dueDate = dueDate.toString(),
    activityType = activityType.name,
    tracking = tracking.toDto(),
    audience = audience.toDto(),
    metadata = metadata.toDto()
)

// error(...) for the not-yet-supported branches is intentional: Phase 2's UI
// never constructs a CountBased/ManualCheckIn tracking or a Groups audience,
// so hitting this would mean a real bug upstream, not a case to swallow.
internal fun TrackingMethod.toDto(): TrackingMethodDto = when (this) {
    is TrackingMethod.TimedWithReflection -> TrackingMethodDto.TimedWithReflection(minDurationSeconds, reflectionMinLength)
    is TrackingMethod.MediaSubmission -> TrackingMethodDto.MediaSubmission(allowedTypes.map { it.name }.toSet(), minDurationSeconds)
    is TrackingMethod.CountBased -> error("CountBased tracking isn't wired up until Phase 5")
    TrackingMethod.ManualCheckIn -> error("ManualCheckIn tracking isn't wired up until Phase 5")
}

internal fun TrackingMethodDto.toDomain(): TrackingMethod = when (this) {
    is TrackingMethodDto.TimedWithReflection -> TrackingMethod.TimedWithReflection(minDurationSeconds, reflectionMinLength)
    is TrackingMethodDto.MediaSubmission -> TrackingMethod.MediaSubmission(allowedTypes.map { MediaType.valueOf(it) }.toSet(), minDurationSeconds)
}

internal fun TaskAudience.toDto(): TaskAudienceDto = when (this) {
    TaskAudience.AllMembers -> TaskAudienceDto.AllMembers
    is TaskAudience.IndividualMembers -> TaskAudienceDto.IndividualMembers(memberIds)
    is TaskAudience.Groups -> TaskAudienceDto.Groups(groupIds)
}

internal fun TaskAudienceDto.toDomain(): TaskAudience = when (this) {
    TaskAudienceDto.AllMembers -> TaskAudience.AllMembers
    is TaskAudienceDto.IndividualMembers -> TaskAudience.IndividualMembers(memberIds)
    is TaskAudienceDto.Groups -> TaskAudience.Groups(groupIds)
}

internal fun TaskMetadata.toDto() = TaskMetadataDto(
    bookTitle = bookTitle,
    pageRange = pageRange?.let { listOf(it.first, it.last) },
    surah = surah,
    ayahRange = ayahRange?.let { listOf(it.first, it.last) },
    customFields = customFields
)

internal fun TaskMetadataDto.toDomain() = TaskMetadata(
    bookTitle = bookTitle,
    pageRange = pageRange?.let { it[0]..it[1] },
    surah = surah,
    ayahRange = ayahRange?.let { it[0]..it[1] },
    customFields = customFields
)

internal fun TaskResponseDto.toDomain() = CircleTask(
    id = id, circleId = circleId, title = title, dueDate = LocalDate.parse(dueDate),
    createdBy = createdBy, activityType = ActivityType.valueOf(activityType),
    tracking = tracking.toDomain(), audience = audience.toDomain(), metadata = metadata.toDomain()
)

internal fun com.studycircle.shared.database.Task.toDomain(
    tracking: TrackingMethodDto, audience: TaskAudienceDto, metadata: TaskMetadataDto
) = CircleTask(
    id = id, circleId = circleId, title = title, dueDate = LocalDate.parse(dueDate),
    createdBy = createdBy, activityType = ActivityType.valueOf(activityType),
    tracking = tracking.toDomain(), audience = audience.toDomain(), metadata = metadata.toDomain()
)
