package com.studycircle.shared.features.task.presentation.review

import com.studycircle.shared.core.mvi.MviEffect
import com.studycircle.shared.core.mvi.MviIntent
import com.studycircle.shared.core.mvi.MviState
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.Submission

sealed interface ReviewQueueIntent : MviIntent {
    data class Load(val taskId: String) : ReviewQueueIntent
    data class Approve(val submissionId: String) : ReviewQueueIntent
    data class Reject(val submissionId: String, val note: String? = null) : ReviewQueueIntent
}

data class ReviewQueueState(
    val taskId: String? = null,
    val submissions: List<Submission> = emptyList(),
    // Context for rendering a submission meaningfully (names instead of ids,
    // the unit/target for count evidence). Best-effort: the queue works without it.
    val task: CircleTask? = null,
    val memberNames: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface ReviewQueueEffect : MviEffect
