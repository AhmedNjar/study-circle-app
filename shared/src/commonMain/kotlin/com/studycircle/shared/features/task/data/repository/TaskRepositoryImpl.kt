package com.studycircle.shared.features.task.data.repository

import com.studycircle.shared.core.network.safeApiCall
import com.studycircle.shared.database.StudyCircleDatabase
import com.studycircle.shared.features.task.data.remote.TaskApi
import com.studycircle.shared.features.task.data.remote.dto.TaskAudienceDto
import com.studycircle.shared.features.task.data.remote.dto.TaskMetadataDto
import com.studycircle.shared.features.task.data.remote.dto.TaskResponseDto
import com.studycircle.shared.features.task.data.remote.dto.TrackingMethodDto
import com.studycircle.shared.features.task.domain.model.CircleTask
import com.studycircle.shared.features.task.domain.model.NewTaskInput
import com.studycircle.shared.features.task.domain.repository.TaskRepository
import kotlinx.serialization.json.Json

class TaskRepositoryImpl(
    private val api: TaskApi,
    private val database: StudyCircleDatabase
) : TaskRepository {
    private val queries = database.taskQueries

    override suspend fun create(circleId: String, task: NewTaskInput): Result<CircleTask> =
        safeApiCall { api.create(circleId, task.toDto()) }.map { dto -> dto.also(::cache).toDomain() }

    override suspend fun listByCircle(circleId: String): Result<List<CircleTask>> {
        val remote = safeApiCall { api.list(circleId) }
        if (remote.isSuccess) {
            return remote.map { list -> list.onEach(::cache).map { it.toDomain() } }
        }
        // Offline fallback — "can't reach the server" shouldn't mean an empty
        // task list when there's a perfectly good cache sitting locally.
        return runCatching {
            queries.selectTasksByCircle(circleId).executeAsList().map { row ->
                row.toDomain(
                    Json.decodeFromString(TrackingMethodDto.serializer(), row.trackingJson),
                    Json.decodeFromString(TaskAudienceDto.serializer(), row.audienceJson),
                    Json.decodeFromString(TaskMetadataDto.serializer(), row.metadataJson)
                )
            }
        }
    }

    override suspend fun getById(taskId: String): Result<CircleTask?> = runCatching {
        queries.selectTaskById(taskId).executeAsOneOrNull()?.let { row ->
            row.toDomain(
                Json.decodeFromString(TrackingMethodDto.serializer(), row.trackingJson),
                Json.decodeFromString(TaskAudienceDto.serializer(), row.audienceJson),
                Json.decodeFromString(TaskMetadataDto.serializer(), row.metadataJson)
            )
        }
    }

    private fun cache(dto: TaskResponseDto) {
        queries.insertOrReplaceTask(
            dto.id, dto.circleId, dto.title, dto.dueDate, dto.createdBy, dto.activityType,
            Json.encodeToString(TrackingMethodDto.serializer(), dto.tracking),
            Json.encodeToString(TaskAudienceDto.serializer(), dto.audience),
            Json.encodeToString(TaskMetadataDto.serializer(), dto.metadata)
        )
    }
}
