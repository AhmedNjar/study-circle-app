package com.studycircle.backend.task.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// Tracking methods mirror the client's TrackingMethod 1:1. Adding a new one is
// a new @JsonSubTypes entry + a branch in SubmissionService.validate() — nothing
// else — which is the point of the composition-over-sealed-hierarchy design.

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TrackingMethodDto.TimedWithReflection::class, name = "TIMED_WITH_REFLECTION"),
    JsonSubTypes.Type(value = TrackingMethodDto.MediaSubmission::class, name = "MEDIA_SUBMISSION"),
    JsonSubTypes.Type(value = TrackingMethodDto.CountBased::class, name = "COUNT_BASED"),
    JsonSubTypes.Type(value = TrackingMethodDto.ManualCheckIn::class, name = "MANUAL_CHECK_IN")
)
sealed class TrackingMethodDto {
    abstract val type: String

    data class TimedWithReflection(
        override val type: String = "TIMED_WITH_REFLECTION",
        val minDurationSeconds: Int,
        val reflectionMinLength: Int
    ) : TrackingMethodDto()

    data class MediaSubmission(
        override val type: String = "MEDIA_SUBMISSION",
        val allowedTypes: Set<String>,
        val minDurationSeconds: Int? = null
    ) : TrackingMethodDto()

    // unit is a free label ("pages", "reps", "km", "ayat"); target must be > 0
    // (enforced in TaskService.create).
    data class CountBased(
        override val type: String = "COUNT_BASED",
        val unit: String,
        val target: Int
    ) : TrackingMethodDto()

    data class ManualCheckIn(override val type: String = "MANUAL_CHECK_IN") : TrackingMethodDto()
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TaskAudienceDto.AllMembers::class, name = "ALL_MEMBERS"),
    JsonSubTypes.Type(value = TaskAudienceDto.IndividualMembers::class, name = "INDIVIDUAL_MEMBERS"),
    JsonSubTypes.Type(value = TaskAudienceDto.Groups::class, name = "GROUPS")
)
sealed class TaskAudienceDto {
    abstract val type: String

    data class AllMembers(override val type: String = "ALL_MEMBERS") : TaskAudienceDto()
    data class IndividualMembers(
        override val type: String = "INDIVIDUAL_MEMBERS",
        val memberIds: List<String>
    ) : TaskAudienceDto()

    // groupIds is never trusted from the client — at task-creation time no
    // groups exist yet (they form afterward via TaskGroupController), so
    // this is always [] on the way in. TaskService.toResponse() overwrites
    // it with a live query on the way out, so it's never stale.
    data class Groups(
        override val type: String = "GROUPS",
        val groupIds: List<String> = emptyList()
    ) : TaskAudienceDto()
}

data class TaskMetadataDto(
    val bookTitle: String? = null,
    val pageRange: List<Int>? = null,
    val surah: String? = null,
    val ayahRange: List<Int>? = null,
    val customFields: Map<String, String> = emptyMap()
)

data class CreateTaskRequest(
    val title: String,
    val dueDate: String,
    val activityType: String,
    val tracking: TrackingMethodDto,
    val audience: TaskAudienceDto,
    val metadata: TaskMetadataDto = TaskMetadataDto()
)

data class TaskResponse(
    val id: String,
    val circleId: String,
    val title: String,
    val dueDate: String,
    val createdBy: String,
    val activityType: String,
    val tracking: TrackingMethodDto,
    val audience: TaskAudienceDto,
    val metadata: TaskMetadataDto
)
