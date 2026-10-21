package com.studycircle.backend.submission.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubmitterRefDto.Individual::class, name = "INDIVIDUAL"),
    JsonSubTypes.Type(value = SubmitterRefDto.OnBehalfOfGroup::class, name = "GROUP")
)
sealed class SubmitterRefDto {
    abstract val type: String
    data class Individual(override val type: String = "INDIVIDUAL", val userId: String) : SubmitterRefDto()
    data class OnBehalfOfGroup(
        override val type: String = "GROUP",
        val groupId: String,
        val submittedByUserId: String
    ) : SubmitterRefDto()
}

// Discriminator values deliberately match TrackingMethodDto's, one-to-one —
// SubmissionService.validate() checks evidence.type against the task's
// tracking.type and rejects a mismatch before anything else.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubmissionEvidenceDto.TimedReflection::class, name = "TIMED_WITH_REFLECTION"),
    JsonSubTypes.Type(value = SubmissionEvidenceDto.Media::class, name = "MEDIA_SUBMISSION"),
    JsonSubTypes.Type(value = SubmissionEvidenceDto.CountBased::class, name = "COUNT_BASED"),
    JsonSubTypes.Type(value = SubmissionEvidenceDto.ManualCheckIn::class, name = "MANUAL_CHECK_IN")
)
sealed class SubmissionEvidenceDto {
    abstract val type: String

    data class TimedReflection(
        override val type: String = "TIMED_WITH_REFLECTION",
        val actualDurationSeconds: Long,
        val reflectionText: String
    ) : SubmissionEvidenceDto()

    data class Media(
        override val type: String = "MEDIA_SUBMISSION",
        val remoteMediaUrl: String,
        val mediaType: String,
        val durationSeconds: Int
    ) : SubmissionEvidenceDto()

    data class CountBased(
        override val type: String = "COUNT_BASED",
        val countAchieved: Int
    ) : SubmissionEvidenceDto()

    data class ManualCheckIn(override val type: String = "MANUAL_CHECK_IN") : SubmissionEvidenceDto()
}

data class SubmitSubmissionRequest(
    val submitter: SubmitterRefDto,
    val evidence: SubmissionEvidenceDto
)

data class SubmissionResponse(
    val id: String,
    val taskId: String,
    val submitter: SubmitterRefDto,
    val submittedAt: String,
    val status: String,
    val supervisorNote: String?,
    val evidence: SubmissionEvidenceDto
)

data class ReviewSubmissionRequest(val status: String, val supervisorNote: String? = null)
