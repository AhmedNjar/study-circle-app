package com.studycircle.backend.streak

import java.time.LocalDate

// Pure day-math for الرِّباط — deliberately no Spring/JPA dependency, so it's
// trivially unit-testable on its own (see StreakMathTest). This same logic
// would be duplicated in the shared KMP module if the client ever needed to
// compute streaks itself, but per the implementation plan the server is the
// sole source of truth for Phase 3 — the client only ever fetches and
// displays, so there's nothing to keep in sync client-side yet.
//
// NOTE: "day" here is the UTC calendar date of submittedAt. A submission at
// 11:58pm/12:02am in the student's own timezone could land on the "wrong"
// side of a UTC day boundary — a known simplification, not an oversight;
// real timezone-aware streak boundaries (the student's local day? the
// circle's?) is a genuinely harder product question worth deciding
// deliberately rather than defaulting into.
object StreakMath {
    data class Result(val currentCount: Int, val bestCount: Int, val lastCompletedDate: LocalDate)

    fun applyCompletion(
        currentCount: Int,
        bestCount: Int,
        lastCompletedDate: LocalDate?,
        completionDate: LocalDate
    ): Result {
        val newCount = when {
            lastCompletedDate == null -> 1
            lastCompletedDate == completionDate -> currentCount // same day already counted — no-op
            lastCompletedDate.plusDays(1) == completionDate -> currentCount + 1
            completionDate.isAfter(lastCompletedDate) -> 1 // gap of 2+ days — streak broke
            else -> {
                // completionDate is BEFORE lastCompletedDate: an older pending
                // submission got approved after a more recent one already
                // advanced the streak (supervisor working through a backlog
                // out of order). Ignore it rather than regressing.
                return Result(currentCount, bestCount, lastCompletedDate)
            }
        }
        return Result(newCount, maxOf(bestCount, newCount), completionDate)
    }
}
