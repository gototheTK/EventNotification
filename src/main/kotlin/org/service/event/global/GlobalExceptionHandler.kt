package org.service.event.global

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    data class ErrorResponse(val status: Int, val error: String, val message: String)

    @ExceptionHandler(EventNotFoundException::class, MemberNotFoundException::class, LikeNotFoundException::class)
    fun handleNotFoundExceptions(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.warn("Not Found Exception: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", e.message ?: ""))
    }

    @ExceptionHandler(DuplicateLikeException::class)
    fun handleConflictExceptions(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.warn("Conflict Exception: ${e.message}")
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), "CONFLICT", e.message ?: ""))
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Internal Server Error: ", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SERVER_ERROR", "서버 내부 오류가 발생했습니다."))
    }
}
