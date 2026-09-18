package com.studycircle.backend.task

import jakarta.persistence.*

enum class GroupCompletionRule { ALL_MUST_SUBMIT, ANY_SUFFICES }

@Entity
@Table(name = "task_group")
class TaskGroupEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val taskId: String,

    var name: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val completionRule: GroupCompletionRule
)

@Entity
@Table(name = "task_group_member")
@IdClass(TaskGroupMemberId::class)
class TaskGroupMemberEntity(
    @Id
    val taskGroupId: String,
    @Id
    val userId: String
)

data class TaskGroupMemberId(val taskGroupId: String = "", val userId: String = "") : java.io.Serializable
