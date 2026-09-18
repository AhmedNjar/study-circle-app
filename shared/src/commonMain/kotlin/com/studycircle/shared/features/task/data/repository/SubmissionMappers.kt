package com.studycircle.shared.features.task.data.repository

import com.studycircle.shared.features.task.data.remote.dto.SubmissionEvidenceDto
import com.studycircle.shared.features.task.data.remote.dto.SubmissionResponseDto
import com.studycircle.shared.features.task.data.remote.dto.SubmitterRefDto
import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.Submission
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.model.SubmitterRef
import kotlinx.datetime.Instant

internal fun SubmitterRef.toDto(): SubmitterRefDto = when (this) {
    is SubmitterRef.Individual -> SubmitterRefDto.Individual(userId)
    is SubmitterRef.OnBehalfOfGroup -> error("Group submissions aren't wired up until Phase 4")
}

internal fun SubmitterRefDto.toDomain(): SubmitterRef = when (this) {
    is SubmitterRefDto.Individual -> SubmitterRef.Individual(userId)
}

internal fun SubmissionEvidence.toDto(): SubmissionEvidenceDto = when (this) {
    is SubmissionEvidence.TimedReflection -> SubmissionEvidenceDto.TimedReflection(actualDurationSeconds, reflectionText)
    is SubmissionEvidence.Media -> SubmissionEvidenceDto.Media(
        remoteMediaUrl = remoteMediaUrl ?: error("Media evidence must be uploaded before it can be sent to the server"),
        mediaType = mediaType.name,
        durationSeconds = durationSeconds
    )
    SubmissionEvidence.CheckIn -> error("CheckIn evidence isn't wired up until Phase 5")
}

internal fun SubmissionEvidenceDto.toDomain(): SubmissionEvidence = when (this) {
    is SubmissionEvidenceDto.TimedReflection -> SubmissionEvidence.TimedReflection(actualDurationSeconds, reflectionText)
    is SubmissionEvidenceDto.Media -> SubmissionEvidence.Media(
        localCachePath = null, // server never knows about the device's local path
        remoteMediaUrl = remoteMediaUrl,
        mediaType = MediaType.valueOf(mediaType),
        durationSeconds = durationSeconds
    )
}

internal fun SubmissionResponseDto.toDomain() = Submission(
    id = id, taskId = taskId, submitter = submitter.toDomain(),
    submittedAt = Instant.parse(submittedAt), status = SubmissionStatus.valueOf(status),
    supervisorNote = supervisorNote, evidence = evidence.toDomain()
)
