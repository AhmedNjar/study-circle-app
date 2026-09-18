package com.studycircle.backend.submission.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubmitterRefDto.Individual::class, name = "INDIVIDUAL")
    // Phase 4 adds SubmitterRefDto.OnBehalfOfGroup here, name = "GROUP"
)
sealed class SubmitterRefDto {
    abstract val type: String
    data class Individual(override val type: String = "INDIVIDUAL", val userId: String) : SubmitterRefDto()
}

// Discriminator values deliberately match TrackingMethodDto's, one-to-one —
// SubmissionService.validate() checks evidence.type against the task's
// tracking.type and rejects a mismatch before anything else.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = SubmissionEvidenceDto.TimedReflection::class, name = "TIMED_WITH_REFLECTION"),
    JsonSubTypes.Type(value = SubmissionEvidenceDto.Media::class, name = "MEDIA_SUBMISSION")
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
