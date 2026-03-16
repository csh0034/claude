package com.ask.claude.ui.member

import com.ask.claude.application.member.fixture.MemberApplicationFixture
import com.ask.claude.application.member.port.CreateMemberUseCase
import com.ask.claude.common.exception.BusinessException
import com.ask.claude.common.exception.ErrorCode
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

// GlobalExceptionHandler(@RestControllerAdvice)는 @WebMvcTest 스캔 범위에 자동 포함됨
@WebMvcTest(MemberController::class)
class MemberControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createMemberUseCase: CreateMemberUseCase

    @Test
    fun `POST members - 201 Created`() {
        val result = MemberApplicationFixture.createMemberResult()
        every { createMemberUseCase.createMember(any()) } returns result

        mockMvc
            .post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "홍길동", "email": "hong@example.com"}"""
            }.andExpect {
                status { isCreated() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { value(1) }
                jsonPath("$.data.name") { value("홍길동") }
                jsonPath("$.data.email") { value("hong@example.com") }
            }
    }

    @Test
    fun `POST members - 400 이름 누락`() {
        mockMvc
            .post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "", "email": "hong@example.com"}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.message") { isNotEmpty() }
            }
    }

    @Test
    fun `POST members - 400 이메일 형식 오류`() {
        mockMvc
            .post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "홍길동", "email": "not-an-email"}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
            }
    }

    @Test
    fun `POST members - 400 필드 누락`() {
        mockMvc
            .post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
            }
    }

    @Test
    fun `POST members - 409 중복 이메일`() {
        every { createMemberUseCase.createMember(any()) } throws BusinessException(ErrorCode.DUPLICATE_EMAIL)

        mockMvc
            .post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"name": "홍길동", "email": "hong@example.com"}"""
            }.andExpect {
                status { isConflict() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.message") { value("이미 사용 중인 이메일입니다") }
            }
    }
}
