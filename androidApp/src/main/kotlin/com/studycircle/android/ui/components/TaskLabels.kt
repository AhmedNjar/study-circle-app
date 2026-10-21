package com.studycircle.android.ui.components

import com.studycircle.shared.features.task.domain.model.ActivityType
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.TrackingMethod
import com.studycircle.shared.localization.ArabicTerms

// Presentation wording derived from task data. Keeps screens free of
// `if (title.contains("تسميع"))` style guesses — what a task IS comes from its
// activityType / tracking, never from its title text or id.

fun activityEmoji(type: ActivityType): String = when (type) {
    ActivityType.READING -> "📖"
    ActivityType.RECITATION -> "🎙"
    ActivityType.WATCHING -> "🎬"
    ActivityType.WORKOUT -> "🏃"
    ActivityType.CUSTOM -> "✦"
}

// CUSTOM tasks carry their own name in metadata (see CreateTaskViewModel).
fun activityLabel(task: CircleTask): String =
    if (task.activityType == ActivityType.CUSTOM) {
        task.metadata.customFields["activityLabel"]?.takeIf { it.isNotBlank() }
            ?: ArabicTerms.activityTypeLabel(ActivityType.CUSTOM)
    } else {
        ArabicTerms.activityTypeLabel(task.activityType)
    }

fun startLabel(type: ActivityType): String = when (type) {
    ActivityType.READING -> "ابدأ القراءة"
    ActivityType.RECITATION -> "ابدأ التسميع"
    ActivityType.WATCHING -> "ابدأ المشاهدة"
    ActivityType.WORKOUT -> "ابدأ التمرين"
    ActivityType.CUSTOM -> "ابدأ المهمة"
}

fun trackingSummary(tracking: TrackingMethod): String = when (tracking) {
    is TrackingMethod.TimedWithReflection -> "${formatMinutes(tracking.minDurationSeconds / 60)} مع ملخص"
    is TrackingMethod.MediaSubmission -> "تسجيل صوتي"
    is TrackingMethod.CountBased -> "${tracking.target.toArabicDigits()} ${tracking.unit}"
    TrackingMethod.ManualCheckIn -> "تأكيد الإنجاز"
}

// One line of activity-specific detail, or null if the task carries none.
fun metadataSummary(task: CircleTask): String? {
    val m = task.metadata
    val parts = buildList {
        m.bookTitle?.takeIf { it.isNotBlank() }?.let { add(it) }
        m.pageRange?.let { add("ص ${it.first.toArabicDigits()}–${it.last.toArabicDigits()}") }
        m.surah?.takeIf { it.isNotBlank() }?.let { add("سورة $it") }
        m.ayahRange?.let { add("الآيات ${it.first.toArabicDigits()}–${it.last.toArabicDigits()}") }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
