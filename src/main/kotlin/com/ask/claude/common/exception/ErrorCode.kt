package com.ask.claude.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val message: String,
    val httpStatus: HttpStatus,
) {
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다", HttpStatus.CONFLICT),
    INVALID_INPUT("잘못된 입력값입니다", HttpStatus.BAD_REQUEST),
}
