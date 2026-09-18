package com.studycircle.shared.features.task.domain.repository

import com.studycircle.shared.features.task.domain.model.TaskGroup

interface TaskGroupRepository {
    // groupId null = create a new group named `name`; groupId set = join
    // that existing group (name is ignored server-side in that case).
    suspend fun formOrJoin(taskId: String, groupId: String?, name: String?): Result<TaskGroup>
    suspend fun listForTask(taskId: String): Result<List<TaskGroup>>
}
