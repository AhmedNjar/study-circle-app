package com.studycircle.shared.features.task.data.remote

import com.studycircle.shared.features.task.data.remote.dto.ReviewSubmissionRequestDto
import com.studycircle.shared.features.task.data.remote.dto.SubmissionResponseDto
import com.studycircle.shared.features.task.data.remote.dto.SubmitSubmissionRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SubmissionApi(private val client: HttpClient) {
    suspend fun submit(taskId: String, request: SubmitSubmissionRequestDto): SubmissionResponseDto =
        client.post("/api/v1/tasks/$taskId/submissions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun listByTask(taskId: String, status: String? = null): List<SubmissionResponseDto> =
        client.get("/api/v1/tasks/$taskId/submissions") {
            status?.let { parameter("status", it) }
        }.body()

    suspend fun review(submissionId: String, request: ReviewSubmissionRequestDto): SubmissionResponseDto =
        client.patch("/api/v1/submissions/$submissionId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
