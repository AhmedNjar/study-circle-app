package com.studycircle.shared.core.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException

// Every repository's remote calls go through this: HttpClientFactory sets
// expectSuccess = true, so a 4xx/5xx throws ResponseException here rather
// than trying (and failing) to deserialize an error body as the success DTO.
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: ResponseException) {
    val parsed = runCatching { e.response.body<ApiErrorBody>() }.getOrNull()
    if (parsed != null) {
        Result.failure(ApiException(parsed.error.code, parsed.error.message))
    } else {
        Result.failure(e)
    }
} catch (e: Exception) {
    Result.failure(e)
}
