package com.ask.claude.ui.exception

import com.ask.claude.common.exception.BusinessException
import com.ask.claude.common.exception.ErrorCode
import com.ask.claude.ui.common.ApiResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(e.errorCode.httpStatus).body(ApiResponse.error(e.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: ErrorCode.INVALID_INPUT.message
        return ResponseEntity.badRequest().body(ApiResponse.error(message))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(e: DataIntegrityViolationException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(ErrorCode.DUPLICATE_EMAIL.httpStatus)
            .body(ApiResponse.error(ErrorCode.DUPLICATE_EMAIL.message))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(ErrorCode.INVALID_INPUT.httpStatus)
            .body(ApiResponse.error(e.message ?: ErrorCode.INVALID_INPUT.message))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 오류가 발생했습니다"))
}
