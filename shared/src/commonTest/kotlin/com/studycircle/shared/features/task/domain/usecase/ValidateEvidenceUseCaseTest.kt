package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.model.MediaType
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValidateEvidenceUseCaseTest {

    private val readingTask = TrackingMethod.TimedWithReflection(minDurationSeconds = 900, reflectionMinLength = 40)

    @Test
    fun rejectsSubmissionBelowMinimumDuration() {
        val evidence = SubmissionEvidence.TimedReflection(
            actualDurationSeconds = 600, // under the 900s floor
            reflectionText = "a".repeat(50)
        )
        assertIs<EvidenceValidationResult.Invalid>(ValidateEvidenceUseCase(readingTask, evidence))
    }

    @Test
    fun rejectsReflectionShorterThanMinimumLength() {
        val evidence = SubmissionEvidence.TimedReflection(
            actualDurationSeconds = 1000,
            reflectionText = "too short" // under 40 chars
        )
        assertIs<EvidenceValidationResult.Invalid>(ValidateEvidenceUseCase(readingTask, evidence))
    }

    @Test
    fun acceptsSubmissionMeetingBothThresholds() {
        val evidence = SubmissionEvidence.TimedReflection(
            actualDurationSeconds = 1000,
            reflectionText = "a".repeat(50)
        )
        assertEquals(EvidenceValidationResult.Valid, ValidateEvidenceUseCase(readingTask, evidence))
    }

    @Test
    fun rejectsEvidenceTypeMismatch() {
        val mediaEvidence = SubmissionEvidence.Media(
            localCachePath = null,
            remoteMediaUrl = "https://example.com/a.m4a",
            mediaType = MediaType.AUDIO,
            durationSeconds = 120
        )
        // A reading task submitted with an audio recording instead of a
        // reflection — this is exactly the class of bug the sealed
        // TrackingMethod/SubmissionEvidence pairing is meant to catch.
        assertIs<EvidenceValidationResult.Invalid>(ValidateEvidenceUseCase(readingTask, mediaEvidence))
    }

    @Test
    fun mediaSubmissionWithNoMinimumDurationAcceptsAnyLength() {
        val recitationTask = TrackingMethod.MediaSubmission(allowedTypes = setOf(MediaType.AUDIO), minDurationSeconds = null)
        val evidence = SubmissionEvidence.Media(
            localCachePath = null,
            remoteMediaUrl = "https://example.com/a.m4a",
            mediaType = MediaType.AUDIO,
            durationSeconds = 5
        )
        assertEquals(EvidenceValidationResult.Valid, ValidateEvidenceUseCase(recitationTask, evidence))
    }

    @Test
    fun rejectsMediaSubmissionBelowMinimumDurationWhenSet() {
        val recitationTask = TrackingMethod.MediaSubmission(allowedTypes = setOf(MediaType.AUDIO), minDurationSeconds = 30)
        val evidence = SubmissionEvidence.Media(
            localCachePath = null,
            remoteMediaUrl = "https://example.com/a.m4a",
            mediaType = MediaType.AUDIO,
            durationSeconds = 10
        )
        assertIs<EvidenceValidationResult.Invalid>(ValidateEvidenceUseCase(recitationTask, evidence))
    }
}
