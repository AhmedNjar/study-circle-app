package com.studycircle.backend.streak

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StreakMathTest {

    @Test
    fun `first ever completion starts the streak at one`() {
        val result = StreakMath.applyCompletion(0, 0, null, LocalDate.of(2026, 9, 1))
        assertEquals(1, result.currentCount)
        assertEquals(1, result.bestCount)
    }

    @Test
    fun `consecutive day increments the streak`() {
        val result = StreakMath.applyCompletion(3, 5, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2))
        assertEquals(4, result.currentCount)
        assertEquals(5, result.bestCount)
    }

    @Test
    fun `same day completion does not double count`() {
        val result = StreakMath.applyCompletion(3, 5, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1))
        assertEquals(3, result.currentCount)
    }

    @Test
    fun `a gap of two or more days resets to one but keeps the best`() {
        val result = StreakMath.applyCompletion(7, 9, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 4))
        assertEquals(1, result.currentCount)
        assertEquals(9, result.bestCount)
    }

    @Test
    fun `a new streak past the previous best updates bestCount`() {
        val result = StreakMath.applyCompletion(4, 4, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2))
        assertEquals(5, result.currentCount)
        assertEquals(5, result.bestCount)
    }

    @Test
    fun `approving an older submission after a newer one does not regress the streak`() {
        // Student's most recently counted day is Sep 3; a Sep 1 submission
        // gets approved late because the supervisor was clearing a backlog.
        val result = StreakMath.applyCompletion(3, 3, LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 1))
        assertEquals(3, result.currentCount)
        assertEquals(LocalDate.of(2026, 9, 3), result.lastCompletedDate)
    }
}
