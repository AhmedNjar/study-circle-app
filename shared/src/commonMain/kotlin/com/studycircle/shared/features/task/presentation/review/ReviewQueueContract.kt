package com.studycircle.shared.features.task.presentation.review

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.task.domain.model.Submission

sealed interface ReviewQueueIntent : MviIntent {
    data class Load(val taskId: String) : ReviewQueueIntent
    data class Approve(val submissionId: String) : ReviewQueueIntent
    data class Reject(val submissionId: String, val note: String? = null) : ReviewQueueIntent
}

data class ReviewQueueState(
    val taskId: String? = null,
    val submissions: List<Submission> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface ReviewQueueEffect : MviEffect
