package com.studycircle.shared.features.circle.data.remote

import com.studycircle.shared.features.circle.data.remote.dto.CircleDetailResponseDto
import com.studycircle.shared.features.circle.data.remote.dto.CircleResponseDto
import com.studycircle.shared.features.circle.data.remote.dto.CreateCircleRequestDto
import com.studycircle.shared.features.circle.data.remote.dto.JoinCircleRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CircleApi(private val client: HttpClient) {
    suspend fun create(request: CreateCircleRequestDto): CircleResponseDto =
        client.post("/api/v1/circles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun join(request: JoinCircleRequestDto): CircleResponseDto =
        client.post("/api/v1/circles/join") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getDetail(circleId: String): CircleDetailResponseDto =
        client.get("/api/v1/circles/$circleId").body()

    suspend fun listMine(): List<CircleResponseDto> =
        client.get("/api/v1/circles").body()
}
