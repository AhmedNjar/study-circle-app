package com.studycircle.shared.features.home.domain.model

import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.model.Session
import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.SubmissionEvidence
import kotlinx.datetime.Instant

// Everything the landing screen renders, assembled once. Arabic-free (see
// ArabicTerms) — the UI layer owns wording and formatting.
data class HomeDashboard(
    val session: Session,
    val circles: List<Circle>,
    val activeTasks: List<HomeTask>,
    // Supervisor-only; always empty for students.
    val pendingReviews: List<PendingReview>,
    // Highest الرِّباط among the user's circles (students only; 0 for supervisors).
    val currentStreak: Int,
    val bestStreak: Int,
    // True when at least one per-circle request failed and its slice is missing —
    // the screen still renders what it has and can offer a retry.
    val isPartial: Boolean
) {
    val isSupervisor: Boolean get() = session.role == Role.SUPERVISOR
}

data class HomeTask(
    val task: CircleTask,
    val circleName: String,
    val streakCount: Int
)

data class PendingReview(
    val submissionId: String,
    val taskId: String,
    val taskTitle: String,
    val circleName: String,
    val submitterName: String,
    val onBehalfOfGroup: Boolean,
    val submittedAt: Instant,
    val evidence: SubmissionEvidence
)
