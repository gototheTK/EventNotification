package org.example.board.handler

import org.example.board.exception.DuplicateLikeException
import org.example.board.exception.EventNotFoundException
import org.example.board.exception.LikeNotFoundException
import org.example.board.exception.MemberNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    // 💡 에러 응답을 담을 표준 포맷 (DTO)
    data class ErrorResponse(val status: Int, val error: String, val message: String)

    // 1. 데이터가 없을 때 발생하는 에러들 (404 Not Found)
    @ExceptionHandler(EventNotFoundException::class, MemberNotFoundException::class, LikeNotFoundException::class)
    fun handleNotFoundExceptions(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.warn("Not Found Exception: ${e.message}")
        val response = ErrorResponse(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", e.message ?: "")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    // 2. 이미 데이터가 존재해서 충돌날 때 (409 Conflict)
    @ExceptionHandler(DuplicateLikeException::class)
    fun handleConflictExceptions(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.warn("Conflict Exception: ${e.message}")
        val response = ErrorResponse(HttpStatus.CONFLICT.value(), "CONFLICT", e.message ?: "")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    // 3. 그 외 예상치 못한 모든 에러 (500 Internal Server Error)
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Internal Server Error: ", e) // 500 에러는 무조건 스택트레이스를 남깁니다.
        val response = ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}