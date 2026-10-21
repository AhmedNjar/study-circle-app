package com.studycircle.shared.features.task.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// kotlinx.serialization's default class discriminator key is "type", which
// is exactly the field name the backend's Jackson polymorphic DTOs use — no
// extra SerializersModule config needed for these to talk to each other.
@Serializable
sealed class TrackingMethodDto {
    @Serializable
    @SerialName("TIMED_WITH_REFLECTION")
    data class TimedWithReflection(val minDurationSeconds: Int, val reflectionMinLength: Int) : TrackingMethodDto()

    @Serializable
    @SerialName("MEDIA_SUBMISSION")
    data class MediaSubmission(val allowedTypes: Set<String>, val minDurationSeconds: Int? = null) : TrackingMethodDto()

    @Serializable
    @SerialName("COUNT_BASED")
    data class CountBased(val unit: String, val target: Int) : TrackingMethodDto()

    @Serializable
    @SerialName("MANUAL_CHECK_IN")
    data object ManualCheckIn : TrackingMethodDto()
}

@Serializable
sealed class TaskAudienceDto {
    @Serializable
    @SerialName("ALL_MEMBERS")
    data object AllMembers : TaskAudienceDto()

    @Serializable
    @SerialName("INDIVIDUAL_MEMBERS")
    data class IndividualMembers(val memberIds: List<String>) : TaskAudienceDto()

    @Serializable
    @SerialName("GROUPS")
    data class Groups(val groupIds: List<String> = emptyList()) : TaskAudienceDto()
}

@Serializable
data class TaskMetadataDto(
    val bookTitle: String? = null,
    val pageRange: List<Int>? = null,
    val surah: String? = null,
    val ayahRange: List<Int>? = null,
    val customFields: Map<String, String> = emptyMap()
)

@Serializable
data class CreateTaskRequestDto(
    val title: String,
    val dueDate: String,
    val activityType: String,
    val tracking: TrackingMethodDto,
    val audience: TaskAudienceDto,
    val metadata: TaskMetadataDto = TaskMetadataDto()
)

@Serializable
data class TaskResponseDto(
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
