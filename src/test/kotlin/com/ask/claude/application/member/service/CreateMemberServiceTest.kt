package com.ask.claude.application.member.service

import com.ask.claude.application.member.fixture.MemberApplicationFixture
import com.ask.claude.common.exception.BusinessException
import com.ask.claude.common.exception.ErrorCode
import com.ask.claude.domain.member.Member
import com.ask.claude.domain.member.MemberPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CreateMemberServiceTest {

    private val memberPort: MemberPort = mockk()
    private val service = CreateMemberService(memberPort)

    @Test
    fun `정상적으로 Member를 생성한다`() {
        val command = MemberApplicationFixture.createMemberCommand()
        val savedMember = Member(id = 1L, name = command.name, email = command.email.lowercase())

        every { memberPort.existsByEmail(command.email.lowercase()) } returns false
        every { memberPort.save(any()) } returns savedMember

        val result = service.createMember(command)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo(command.name)
        assertThat(result.email).isEqualTo(command.email.lowercase())
        verify(exactly = 1) {
            memberPort.save(match { it.name == command.name && it.email == command.email.lowercase() })
        }
    }

    @Test
    fun `이메일을 소문자로 정규화하여 저장한다`() {
        val command = MemberApplicationFixture.createMemberCommand(email = "Hong@Example.COM")
        val savedMember = Member(id = 1L, name = command.name, email = "hong@example.com")

        every { memberPort.existsByEmail("hong@example.com") } returns false
        every { memberPort.save(any()) } returns savedMember

        val result = service.createMember(command)

        assertThat(result.email).isEqualTo("hong@example.com")
        verify(exactly = 1) {
            memberPort.existsByEmail("hong@example.com")
        }
    }

    @Test
    fun `중복 이메일이면 BusinessException을 던진다`() {
        val command = MemberApplicationFixture.createMemberCommand()

        every { memberPort.existsByEmail(command.email.lowercase()) } returns true

        assertThatThrownBy { service.createMember(command) }
            .isInstanceOf(BusinessException::class.java)
            .satisfies({ ex ->
                val be = ex as BusinessException
                assertThat(be.errorCode).isEqualTo(ErrorCode.DUPLICATE_EMAIL)
                assertThat(be.message).isEqualTo(ErrorCode.DUPLICATE_EMAIL.message)
            })
        verify(exactly = 0) { memberPort.save(any()) }
    }
}
