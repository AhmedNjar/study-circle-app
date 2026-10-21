package com.studycircle.shared.features.task.domain.usecase

import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import com.studycircle.shared.features.task.domain.model.TrackingMethod

sealed class EvidenceValidationResult {
    data object Valid : EvidenceValidationResult()
    data class Invalid(val reason: String) : EvidenceValidationResult()
}

// The anti-idle-cheating guard from the original spec, enforced client-side
// before a submission ever touches SQLDelight or the network — the backend's
// SubmissionService runs the identical rule (never trust the client), but
// this is what gives the student instant feedback instead of a round trip.
// Pure function, no framework dependency — this is the "cheap and high-value
// unit test" the implementation plan called out for Phase 2.
object ValidateEvidenceUseCase {
    operator fun invoke(tracking: TrackingMethod, evidence: SubmissionEvidence): EvidenceValidationResult =
        when (tracking) {
            is TrackingMethod.TimedWithReflection -> validateTimedReflection(tracking, evidence)
            is TrackingMethod.MediaSubmission -> validateMedia(tracking, evidence)
            is TrackingMethod.CountBased -> validateCount(tracking, evidence)
            TrackingMethod.ManualCheckIn ->
                if (evidence is SubmissionEvidence.CheckIn) EvidenceValidationResult.Valid
                else EvidenceValidationResult.Invalid("نوع الدليل غير متطابق مع نوع المهمة")
        }

    private fun validateCount(
        tracking: TrackingMethod.CountBased,
        evidence: SubmissionEvidence
    ): EvidenceValidationResult {
        val ev = evidence as? SubmissionEvidence.Count
            ?: return EvidenceValidationResult.Invalid("نوع الدليل غير متطابق مع نوع المهمة")
        return if (ev.countAchieved < tracking.target) {
            EvidenceValidationResult.Invalid("العدد المُنجز أقل من الهدف المطلوب")
        } else {
            EvidenceValidationResult.Valid
        }
    }

    private fun validateTimedReflection(
        tracking: TrackingMethod.TimedWithReflection,
        evidence: SubmissionEvidence
    ): EvidenceValidationResult {
        val ev = evidence as? SubmissionEvidence.TimedReflection
            ?: return EvidenceValidationResult.Invalid("نوع الدليل غير متطابق مع نوع المهمة")
        return when {
            ev.actualDurationSeconds < tracking.minDurationSeconds ->
                EvidenceValidationResult.Invalid("مدة القراءة أقل من الحد الأدنى المطلوب")
            ev.reflectionText.length < tracking.reflectionMinLength ->
                EvidenceValidationResult.Invalid("الملخص أقصر من الحد الأدنى المطلوب")
            else -> EvidenceValidationResult.Valid
        }
    }

    private fun validateMedia(
        tracking: TrackingMethod.MediaSubmission,
        evidence: SubmissionEvidence
    ): EvidenceValidationResult {
        val ev = evidence as? SubmissionEvidence.Media
            ?: return EvidenceValidationResult.Invalid("نوع الدليل غير متطابق مع نوع المهمة")
        val minDuration = tracking.minDurationSeconds
        return if (minDuration != null && ev.durationSeconds < minDuration) {
            EvidenceValidationResult.Invalid("مدة التسجيل أقل من الحد الأدنى المطلوب")
        } else {
            EvidenceValidationResult.Valid
        }
    }
}
