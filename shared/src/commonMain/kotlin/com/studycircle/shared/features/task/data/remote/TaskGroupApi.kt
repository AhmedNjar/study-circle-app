package com.studycircle.shared.features.task.data.remote

import com.studycircle.shared.features.task.data.remote.dto.FormOrJoinGroupRequestDto
import com.studycircle.shared.features.task.data.remote.dto.TaskGroupResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TaskGroupApi(private val client: HttpClient) {
    suspend fun formOrJoin(taskId: String, request: FormOrJoinGroupRequestDto): TaskGroupResponseDto =
        client.post("/api/v1/tasks/$taskId/groups") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun list(taskId: String): List<TaskGroupResponseDto> =
        client.get("/api/v1/tasks/$taskId/groups").body()
}
