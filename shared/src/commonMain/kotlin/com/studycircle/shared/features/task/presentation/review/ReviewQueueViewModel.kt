package com.studycircle.shared.features.task.presentation.review

import com.studycircle.shared.core.mvi.MviViewModel
import com.studycircle.shared.features.task.domain.model.SubmissionStatus
import com.studycircle.shared.features.circle.domain.usecase.GetCircleUseCase
import com.studycircle.shared.features.task.domain.usecase.GetTaskUseCase
import com.studycircle.shared.features.task.domain.usecase.ListSubmissionsUseCase
import com.studycircle.shared.features.task.domain.usecase.ReviewSubmissionUseCase
import kotlinx.coroutines.CoroutineScope

class ReviewQueueViewModel(
    private val listSubmissionsUseCase: ListSubmissionsUseCase,
    private val reviewSubmissionUseCase: ReviewSubmissionUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val getCircleUseCase: GetCircleUseCase,
    scope: CoroutineScope
) : MviViewModel<ReviewQueueIntent, ReviewQueueState, ReviewQueueEffect>(ReviewQueueState(), scope) {

    override suspend fun handleIntent(intent: ReviewQueueIntent) {
        when (intent) {
            is ReviewQueueIntent.Load -> load(intent.taskId)
            is ReviewQueueIntent.Approve -> review(intent.submissionId, SubmissionStatus.APPROVED, null)
            is ReviewQueueIntent.Reject -> review(intent.submissionId, SubmissionStatus.REJECTED, intent.note)
        }
    }

    private suspend fun load(taskId: String) {
        setState { copy(isLoading = true, errorMessage = null, taskId = taskId) }
        listSubmissionsUseCase(taskId, SubmissionStatus.PENDING).fold(
            onSuccess = { list -> setState { copy(isLoading = false, submissions = list) } },
            onFailure = { setState { copy(isLoading = false, errorMessage = "تعذر تحميل قائمة المراجعة") } }
        )
        loadContext(taskId)
    }

    // Names + task details, fetched once per task. A failure here only costs
    // prettier labels, so it never surfaces as an error.
    private suspend fun loadContext(taskId: String) {
        if (currentState.task?.id == taskId) return
        val task = getTaskUseCase(taskId).getOrNull() ?: return
        val names = getCircleUseCase(task.circleId).getOrNull()
            ?.members?.associate { it.id to it.displayName }
            .orEmpty()
        setState { copy(task = task, memberNames = names) }
    }

    private suspend fun review(submissionId: String, status: SubmissionStatus, note: String?) {
        reviewSubmissionUseCase(submissionId, status, note).fold(
            onSuccess = { currentState.taskId?.let { load(it) } }, // refresh the queue after a decision
            onFailure = { setState { copy(errorMessage = "تعذر تحديث حالة المهمة") } }
        )
    }
}
