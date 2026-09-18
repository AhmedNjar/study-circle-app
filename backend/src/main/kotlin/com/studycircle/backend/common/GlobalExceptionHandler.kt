package com.studycircle.backend.common

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorBody(val code: String, val message: String)
data class ErrorEnvelope(val error: ErrorBody)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorEnvelope> =
        ResponseEntity.status(ex.status).body(ErrorEnvelope(ErrorBody(ex.code, ex.message ?: ex.code)))
}
