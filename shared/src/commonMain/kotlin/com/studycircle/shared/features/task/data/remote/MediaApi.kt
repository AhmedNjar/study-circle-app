package com.studycircle.shared.features.task.data.remote

import com.studycircle.shared.features.task.data.remote.dto.UploadUrlRequestDto
import com.studycircle.shared.features.task.data.remote.dto.UploadUrlResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MediaApi(private val client: HttpClient) {
    suspend fun getUploadUrl(request: UploadUrlRequestDto): UploadUrlResponseDto =
        client.post("/api/v1/media/upload-url") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // uploadUrl is always the relative path getUploadUrl() just returned, so
    // it resolves against the same host as everything else — see the
    // HttpClientFactory comment on why paths must stay host-relative.
    suspend fun uploadBytes(uploadUrl: String, bytes: ByteArray) {
        client.put(uploadUrl) {
            contentType(ContentType.Application.OctetStream)
            setBody(bytes)
        }
    }
}
