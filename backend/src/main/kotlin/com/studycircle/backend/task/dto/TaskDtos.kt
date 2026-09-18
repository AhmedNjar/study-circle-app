package com.studycircle.backend.task.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// Phase 2 only wires up two tracking methods and two audience shapes — the
// ones the original spec named concretely (book reading, Quran recitation;
// everyone or specific people). CountBased/ManualCheckIn and Groups slot in
// as new @JsonSubTypes entries in Phase 4/5 without touching anything else,
// which is the whole point of the composition-over-sealed-hierarchy design
// from step 1.

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TrackingMethodDto.TimedWithReflection::class, name = "TIMED_WITH_REFLECTION"),
    JsonSubTypes.Type(value = TrackingMethodDto.MediaSubmission::class, name = "MEDIA_SUBMISSION")
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
