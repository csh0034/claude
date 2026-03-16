package com.ask.claude.domain.member

import com.ask.claude.domain.member.fixture.MemberDomainFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MemberTest {
    @Test
    fun `정상적인 Member 생성`() {
        val member = MemberDomainFixture.savedMember()

        assertThat(member.id).isEqualTo(1L)
        assertThat(member.name).isEqualTo("홍길동")
        assertThat(member.email).isEqualTo("hong@example.com")
    }

    @Test
    fun `저장 전 Member는 id가 0이다`() {
        val member = MemberDomainFixture.unsavedMember()

        assertThat(member.id).isEqualTo(0L)
    }

    @Test
    fun `이름이 빈 문자열이면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(name = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이름은 필수입니다")
    }

    @Test
    fun `이름이 공백이면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(name = "   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `이름이 100자를 초과하면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(name = "가".repeat(101)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("100자")
    }

    @Test
    fun `이름이 정확히 100자이면 정상 생성`() {
        val member = MemberDomainFixture.createMember(name = "가".repeat(100))

        assertThat(member.name).hasSize(100)
    }

    @Test
    fun `이메일이 빈 문자열이면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(email = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이메일은 필수입니다")
    }

    @Test
    fun `이메일 형식이 올바르지 않으면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(email = "not-an-email") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("이메일 형식")
    }

    @Test
    fun `이메일에 공백이 포함되면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(email = "hong @example.com") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `이메일 TLD가 1자이면 예외 발생`() {
        assertThatThrownBy { MemberDomainFixture.createMember(email = "hong@example.c") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `이메일에 플러스 기호가 포함되면 정상 생성`() {
        val member = MemberDomainFixture.createMember(email = "hong+test@example.com")

        assertThat(member.email).isEqualTo("hong+test@example.com")
    }

    @Test
    fun `이메일이 254자를 초과하면 예외 발생`() {
        val longEmail = "a".repeat(243) + "@example.com" // 255자
        assertThatThrownBy { MemberDomainFixture.createMember(email = longEmail) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("254자")
    }
}
