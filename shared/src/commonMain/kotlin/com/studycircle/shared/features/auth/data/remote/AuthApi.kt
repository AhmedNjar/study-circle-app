package com.studycircle.shared.features.auth.data.remote

import com.studycircle.shared.features.auth.data.remote.dto.AuthResponseDto
import com.studycircle.shared.features.auth.data.remote.dto.LoginRequestDto
import com.studycircle.shared.features.auth.data.remote.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(private val client: HttpClient) {
    suspend fun register(request: RegisterRequestDto): AuthResponseDto =
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun login(request: LoginRequestDto): AuthResponseDto =
        client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
