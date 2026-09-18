package com.studycircle.shared.core.network

import kotlinx.serialization.Serializable

// Mirrors the backend's GlobalExceptionHandler error envelope exactly:
// { "error": { "code": "...", "message": "..." } }
@Serializable
data class ApiErrorBody(val error: ApiErrorDetail)

@Serializable
data class ApiErrorDetail(val code: String, val message: String)

// What safeApiCall wraps a parsed error into. ViewModels match on `code`
// (e.g. "INVALID_CREDENTIALS", "CIRCLE_NOT_FOUND") to show the right message
// rather than a generic failure string.
class ApiException(val code: String, message: String) : Exception(message)
