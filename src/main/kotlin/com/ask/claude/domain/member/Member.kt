package com.ask.claude.domain.member

class Member(
    val id: Long = 0L,
    val name: String,
    val email: String,
) {
    init {
        require(name.isNotBlank()) { "이름은 필수입니다" }
        require(name.length <= 100) { "이름은 100자 이하여야 합니다" }
        require(email.isNotBlank()) { "이메일은 필수입니다" }
        require(email.length <= 254) { "이메일은 254자 이하여야 합니다" }
        require(email.matches(EMAIL_REGEX)) { "이메일 형식이 올바르지 않습니다" }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
