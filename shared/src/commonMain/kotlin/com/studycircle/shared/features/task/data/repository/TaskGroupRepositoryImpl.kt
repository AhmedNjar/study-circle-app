package com.studycircle.shared.features.task.data.repository

import com.studycircle.shared.core.network.safeApiCall
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.task.data.remote.TaskGroupApi
import com.studycircle.shared.features.task.data.remote.dto.FormOrJoinGroupRequestDto
import com.studycircle.shared.features.task.data.remote.dto.TaskGroupResponseDto
import com.studycircle.shared.features.task.domain.model.GroupCompletionRule
import com.studycircle.shared.features.task.domain.model.TaskGroup
import com.studycircle.shared.features.task.domain.repository.TaskGroupRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Shares database.taskQueries with TaskRepositoryImpl — TaskGroupEntity is
// defined in the same Task.sq file as Task itself (see step-1's schema),
// so SQLDelight generates one Queries class covering both tables.
class TaskGroupRepositoryImpl(
    private val api: TaskGroupApi,
    private val database: StudyCircleDatabase
) : TaskGroupRepository {
    private val queries = database.taskQueries

    override suspend fun formOrJoin(taskId: String, groupId: String?, name: String?): Result<TaskGroup> =
        safeApiCall { api.formOrJoin(taskId, FormOrJoinGroupRequestDto(groupId, name)) }
            .map { dto -> dto.also(::cache).toDomain() }

    override suspend fun listForTask(taskId: String): Result<List<TaskGroup>> {
        val remote = safeApiCall { api.list(taskId) }
        if (remote.isSuccess) {
            return remote.map { list -> list.onEach(::cache).map { it.toDomain() } }
        }
        // Offline fallback, same pattern as TaskRepositoryImpl.listByCircle.
        return runCatching {
            queries.selectGroupsByTask(taskId).executeAsList().map { row ->
                TaskGroup(
                    id = row.id, taskId = row.taskId, name = row.name,
                    memberIds = Json.decodeFromString(row.memberIdsJson),
                    completionRule = GroupCompletionRule.valueOf(row.completionRule)
                )
            }
        }
    }

    private fun cache(dto: TaskGroupResponseDto) {
        queries.insertOrReplaceGroup(dto.id, dto.taskId, dto.name, Json.encodeToString(dto.memberIds), dto.completionRule)
    }

    private fun TaskGroupResponseDto.toDomain() = TaskGroup(
        id = id, taskId = taskId, name = name, memberIds = memberIds,
        completionRule = GroupCompletionRule.valueOf(completionRule)
    )
}
