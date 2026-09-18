package com.studycircle.shared.features.task.domain.repository

import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.NewTaskInput

interface TaskRepository {
    suspend fun create(circleId: String, task: NewTaskInput): Result<CircleTask>
    suspend fun listByCircle(circleId: String): Result<List<CircleTask>>
    suspend fun getById(taskId: String): Result<CircleTask?>
}
