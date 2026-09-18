package com.studycircle.shared.localization

// Presentation-layer only — never referenced from domain or data. This is the
// single place UI terminology lives, so it can change per-circle later
// without touching business logic.
import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.SubmissionStatus

object ArabicTerms {
    fun roleLabel(role: Role) = when (role) {
        Role.SUPERVISOR -> "المشرف"
        Role.STUDENT -> "السالك"
    }

    fun activityTypeLabel(type: ActivityType) = when (type) {
        ActivityType.READING -> "قراءة"
        ActivityType.RECITATION -> "تسميع"
        ActivityType.WATCHING -> "مشاهدة"
        ActivityType.WORKOUT -> "تمرين"
        ActivityType.CUSTOM -> "نشاط مخصص"
    }

    fun submissionStatusLabel(status: SubmissionStatus) = when (status) {
        SubmissionStatus.PENDING -> "قيد المراجعة"
        SubmissionStatus.APPROVED -> "مقبول"
        SubmissionStatus.REJECTED -> "مرفوض"
        SubmissionStatus.NEEDS_REVISION -> "يحتاج تعديل"
    }

    const val STREAK_LABEL = "الرِّباط"
    const val CIRCLE_LABEL = "الحلقة"
    const val GROUP_LABEL = "مجموعة"
    const val INDIVIDUAL_LABEL = "فردي"
}
