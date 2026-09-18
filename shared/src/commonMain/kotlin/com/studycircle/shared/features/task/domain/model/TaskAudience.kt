package com.studycircle.shared.features.task.domain.model

// A task can target everyone, specific individuals, or one/more groups of
// members who intend to do it together.
sealed class TaskAudience {
    data object AllMembers : TaskAudience()
    data class IndividualMembers(val memberIds: List<String>) : TaskAudience()
    data class Groups(val groupIds: List<String>) : TaskAudience()
}

enum class GroupCompletionRule { ALL_MUST_SUBMIT, ANY_SUFFICES }

// Its own entity (own id, own lifecycle) — members can form/join it after the
// task is created, up until the due date.
data class TaskGroup(
    val id: String,
    val taskId: String,
    val name: String?,
    val memberIds: List<String>,
    val completionRule: GroupCompletionRule = GroupCompletionRule.ALL_MUST_SUBMIT
)
