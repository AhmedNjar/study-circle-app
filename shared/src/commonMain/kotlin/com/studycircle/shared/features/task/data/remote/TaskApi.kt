package com.studycircle.shared.features.task.data.remote

import com.studycircle.shared.features.task.data.remote.dto.CreateTaskRequestDto
import com.studycircle.shared.features.task.data.remote.dto.TaskResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TaskApi(private val client: HttpClient) {
    suspend fun create(circleId: String, request: CreateTaskRequestDto): TaskResponseDto =
        client.post("/api/v1/circles/$circleId/tasks") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun list(circleId: String): List<TaskResponseDto> =
        client.get("/api/v1/circles/$circleId/tasks").body()
}
