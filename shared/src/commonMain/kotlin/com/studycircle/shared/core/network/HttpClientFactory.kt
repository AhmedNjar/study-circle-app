package com.studycircle.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    // Host + port only — endpoints below always pass the full "/api/v1/..."
    // path. (Ktor's default-request URL resolution replaces the whole path
    // when a request URL starts with "/", so baking "/api/v1" in here would
    // silently get overwritten rather than prefixed.)
    // TODO Phase 2+: move to a build-config value per environment.
    private const val BASE_URL = "http://localhost:8080"

    fun create(tokenProvider: TokenProvider): HttpClient = HttpClient {
        // Lets safeApiCall() catch ResponseException on 4xx/5xx instead of a
        // failed JSON deserialization when the body doesn't match the DTO.
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    tokenProvider.currentToken()?.let { BearerTokens(it, it) }
                }
                // No refresh-token flow in Phase 1's backend yet — an expired
                // token just fails the request; add refreshTokens{} here once
                // the backend issues one.
            }
        }
        defaultRequest {
            url(BASE_URL)
        }
    }
}
