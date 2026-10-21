package com.studycircle.shared.features.task.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SubmitterRefDto {
    @Serializable
    @SerialName("INDIVIDUAL")
    data class Individual(val userId: String) : SubmitterRefDto()

    @Serializable
    @SerialName("GROUP")
    data class OnBehalfOfGroup(val groupId: String, val submittedByUserId: String) : SubmitterRefDto()
}

// Discriminator values deliberately match TrackingMethodDto's, one-to-one.
@Serializable
sealed class SubmissionEvidenceDto {
    @Serializable
    @SerialName("TIMED_WITH_REFLECTION")
    data class TimedReflection(val actualDurationSeconds: Long, val reflectionText: String) : SubmissionEvidenceDto()

    @Serializable
    @SerialName("MEDIA_SUBMISSION")
    data class Media(val remoteMediaUrl: String, val mediaType: String, val durationSeconds: Int) : SubmissionEvidenceDto()

    @Serializable
    @SerialName("COUNT_BASED")
    data class CountBased(val countAchieved: Int) : SubmissionEvidenceDto()

    @Serializable
    @SerialName("MANUAL_CHECK_IN")
    data object ManualCheckIn : SubmissionEvidenceDto()
}

@Serializable
data class SubmitSubmissionRequestDto(val submitter: SubmitterRefDto, val evidence: SubmissionEvidenceDto)

@Serializable
data class SubmissionResponseDto(
    val id: String,
    val taskId: String,
    val submitter: SubmitterRefDto,
    val submittedAt: String,
    val status: String,
    val supervisorNote: String? = null,
    val evidence: SubmissionEvidenceDto
)

@Serializable
data class ReviewSubmissionRequestDto(val status: String, val supervisorNote: String? = null)
