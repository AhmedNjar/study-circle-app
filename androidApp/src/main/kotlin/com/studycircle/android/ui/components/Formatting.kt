package com.studycircle.android.ui.components

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

// Arabic-Indic digits and Arabic date/time wording, shared by every screen so the
// whole app reads consistently (the redesign uses ٠-٩ everywhere). Deliberately
// not Locale/NumberFormat based: the digit shape those produce depends on the
// device's region and would make the same screen look different per phone.

private const val ARABIC_ZERO = '٠'

fun String.toArabicDigits(): String =
    map { if (it in '0'..'9') ARABIC_ZERO + (it - '0') else it }.joinToString("")

fun Int.toArabicDigits(): String = toString().toArabicDigits()
fun Long.toArabicDigits(): String = toString().toArabicDigits()

private val GREGORIAN_MONTHS_AR = listOf(
    "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
)

// "٢٠ أكتوبر ٢٠٢٦"
fun LocalDate.toArabicLong(): String =
    "${dayOfMonth.toArabicDigits()} ${GREGORIAN_MONTHS_AR[monthNumber - 1]} ${year.toArabicDigits()}"

// "٠٥:٠٩" — countdown/stopwatch display.
fun formatClock(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val m = (safe / 60).toString().padStart(2, '0')
    val s = (safe % 60).toString().padStart(2, '0')
    return "$m:$s".toArabicDigits()
}

// Arabic number agreement: 1 → singular, 2 → dual, 3-10 → plural, 11+ → singular.
private fun agree(n: Long, one: String, two: String, few: String, many: String): String = when {
    n == 1L -> one
    n == 2L -> two
    n in 3..10 -> "${n.toArabicDigits()} $few"
    else -> "${n.toArabicDigits()} $many"
}

// "منذ ٣ ساعات"
fun formatTimeAgo(from: Instant, now: Instant = Clock.System.now()): String {
    val seconds = (now - from).inWholeSeconds
    if (seconds < 60) return "الآن"
    val minutes = seconds / 60
    if (minutes < 60) return "منذ " + agree(minutes, "دقيقة", "دقيقتين", "دقائق", "دقيقة")
    val hours = minutes / 60
    if (hours < 24) return "منذ " + agree(hours, "ساعة", "ساعتين", "ساعات", "ساعة")
    val days = hours / 24
    return "منذ " + agree(days, "يوم", "يومين", "أيام", "يومًا")
}

// Short Arabic duration for task requirements: "٣٠ دقيقة", "ساعة", "ساعتان".
fun formatMinutes(totalMinutes: Int): String =
    agree(totalMinutes.toLong(), "دقيقة", "دقيقتان", "دقائق", "دقيقة")

// Two-letter avatar initials from a display name ("خالد المنصور" → "خم").
fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "؟"
        parts.size == 1 -> parts[0].take(1)
        else -> parts[0].take(1) + parts[1].take(1)
    }
}
