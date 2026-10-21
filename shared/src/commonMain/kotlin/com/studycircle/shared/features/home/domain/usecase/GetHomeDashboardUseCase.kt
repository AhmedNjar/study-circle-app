package com.studycircle.shared.features.home.domain.usecase

import com.studycircle.shared.features.auth.domain.model.Role
import com.studycircle.shared.features.auth.domain.model.Session
import com.studycircle.shared.features.auth.domain.repository.AuthRepository
import com.studycircle.shared.features.circle.domain.model.Circle
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import com.studycircle.shared.features.circle.domain.usecase.ListMyCirclesUseCase
import com.studycircle.shared.features.home.domain.model.HomeDashboard
import com.studycircle.shared.features.home.domain.model.HomeTask
import com.studycircle.shared.features.home.domain.model.PendingReview
import com.studycircle.shared.features.streak.domain.model.Streak
import com.studycircle.shared.features.streak.domain.model.StreakOwner
import com.studycircle.shared.features.streak.domain.usecase.GetStreakUseCase
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.task.domain.model.SubmitterRef
import com.studycircle.shared.features.task.domain.model.TaskAudience
import com.studycircle.shared.features.task.domain.usecase.ListSubmissionsUseCase
import com.studycircle.shared.features.task.domain.usecase.ListTasksUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Aggregates the existing per-feature use cases into one screen model.
//
// Known cost: this fans out N requests per circle (tasks, streak, and — for a
// supervisor — circle detail + one pending-submissions call per task). Fine for
// an MVP-sized account; the intended next step is a single backend
// GET /home endpoint. Because the UI only ever sees HomeDashboard, swapping the
// implementation behind this class won't touch any screen.
class GetHomeDashboardUseCase(
    private val auth: AuthRepository,
    private val listMyCircles: ListMyCirclesUseCase,
    private val listTasks: ListTasksUseCase,
    private val listSubmissions: ListSubmissionsUseCase,
    private val getCircle: GetCircleUseCase,
    private val getStreak: GetStreakUseCase,
    private val clock: Clock = Clock.System
) {
    private class CircleSlice(
        val circle: Circle,
        val tasks: List<CircleTask>,
        val streak: Streak?,
        val pending: List<PendingReview>,
        val failed: Boolean
    )

    suspend operator fun invoke(): Result<HomeDashboard> {
        val session = auth.currentSession()
            ?: return Result.failure(IllegalStateException("No active session"))
        val circles = listMyCircles().getOrElse { return Result.failure(it) }
        val today = clock.todayIn(TimeZone.currentSystemDefault())

        // Results (not shared mutable flags) come back through async so concurrent
        // coroutines never write to the same variable.
        val slices = coroutineScope {
            circles.map { circle -> async { loadSlice(session, circle) } }.awaitAll()
        }

        val activeTasks = slices
            .flatMap { slice ->
                slice.tasks
                    .filter { it.dueDate >= today && isVisibleTo(session, it) }
                    .map { HomeTask(it, slice.circle.name, slice.streak?.currentCount ?: 0) }
            }
            .sortedBy { it.task.dueDate }

        return Result.success(
            HomeDashboard(
                session = session,
                circles = circles,
                activeTasks = activeTasks,
                pendingReviews = slices.flatMap { it.pending }.sortedByDescending { it.submittedAt },
                currentStreak = slices.maxOfOrNull { it.streak?.currentCount ?: 0 } ?: 0,
                bestStreak = slices.maxOfOrNull { it.streak?.bestCount ?: 0 } ?: 0,
                isPartial = slices.any { it.failed }
            )
        )
    }

    private suspend fun loadSlice(session: Session, circle: Circle): CircleSlice = coroutineScope {
        val supervises = circle.supervisorId == session.userId
        val tasksDeferred = async { listTasks(circle.id) }
        // Supervisors don't submit, so they have no personal الرِّباط to show.
        val streakDeferred = async {
            if (session.role == Role.STUDENT) getStreak(circle.id, StreakOwner.Individual(session.userId)) else null
        }
        // Member names are only needed to label pending submissions.
        val detailDeferred = async { if (supervises) getCircle(circle.id) else null }

        val tasksResult = tasksDeferred.await()
        val tasks = tasksResult.getOrNull().orEmpty()
        var failed = tasksResult.isFailure

        // A missing streak is cosmetic (new user, or endpoint hiccup) → shown as 0,
        // not treated as a failed slice.
        val streak = streakDeferred.await()?.getOrNull()

        val pending = if (supervises) {
            val names = detailDeferred.await()?.getOrNull()
                ?.members?.associate { it.id to it.displayName }
                .orEmpty()
            tasks
                .map { task -> async { task to listSubmissions(task.id, SubmissionStatus.PENDING) } }
                .awaitAll()
                .flatMap { (task, result) ->
                    if (result.isFailure) failed = true
                    result.getOrNull().orEmpty().map { submission ->
                        val (userId, isGroup) = when (val ref = submission.submitter) {
                            is SubmitterRef.Individual -> ref.userId to false
                            is SubmitterRef.OnBehalfOfGroup -> ref.submittedByUserId to true
                        }
                        PendingReview(
                            submissionId = submission.id,
                            taskId = task.id,
                            taskTitle = task.title,
                            circleName = circle.name,
                            submitterName = names[userId] ?: userId,
                            onBehalfOfGroup = isGroup,
                            submittedAt = submission.submittedAt,
                            evidence = submission.evidence
                        )
                    }
                }
        } else {
            emptyList()
        }

        CircleSlice(circle, tasks, streak, pending, failed)
    }

    // A supervisor sees everything in their circles. A student sees tasks aimed at
    // everyone, at groups (which they can join), or at them by name.
    private fun isVisibleTo(session: Session, task: CircleTask): Boolean = when {
        session.role == Role.SUPERVISOR -> true
        else -> when (val audience = task.audience) {
            TaskAudience.AllMembers -> true
            is TaskAudience.Groups -> true
            is TaskAudience.IndividualMembers -> session.userId in audience.memberIds
        }
    }
}
