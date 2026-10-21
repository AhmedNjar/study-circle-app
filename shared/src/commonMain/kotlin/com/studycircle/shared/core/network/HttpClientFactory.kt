package com.studycircle.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Environment-specific, supplied by each platform shell (Android: BuildConfig,
// iOS: its own config). Host + port only — endpoints always pass the full
// "/api/v1/..." path. (Ktor's default-request URL resolution replaces the whole
// path when a request URL starts with "/", so baking "/api/v1" in here would
// silently get overwritten rather than prefixed.)
data class ApiConfig(val baseUrl: String)

object HttpClientFactory {

    // Reads the token on EVERY request instead of using Ktor's Auth/bearer
    // plugin. That plugin caches whatever loadTokens{} returned first, so after
    // logout → login as a different user it would keep sending the previous
    // user's token. There's no refresh-token flow on the backend yet, so the
    // caching bought nothing; a per-request read of the (local SQLite) session
    // row is cheap and always correct.
    private fun bearerHeaderPlugin(tokenProvider: TokenProvider) =
        createClientPlugin("BearerHeader") {
            onRequest { request, _ ->
                tokenProvider.currentToken()?.let { request.header(HttpHeaders.Authorization, "Bearer $it") }
            }
        }

    fun create(tokenProvider: TokenProvider, config: ApiConfig): HttpClient = HttpClient {
        // Lets safeApiCall() catch ResponseException on 4xx/5xx instead of a
        // failed JSON deserialization when the body doesn't match the DTO.
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(bearerHeaderPlugin(tokenProvider))
        defaultRequest {
            url(config.baseUrl)
        }
    }
}
