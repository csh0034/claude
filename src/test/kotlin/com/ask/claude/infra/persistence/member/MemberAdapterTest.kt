package com.ask.claude.infra.persistence.member

import com.ask.claude.domain.member.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(MemberAdapter::class, MemberMapper::class)
class MemberAdapterTest {
    @Autowired
    private lateinit var memberAdapter: MemberAdapter

    @Test
    fun `Member를 저장하면 id가 채워진 Domain Member를 반환한다`() {
        val member = Member(name = "홍길동", email = "hong@example.com")

        val saved = memberAdapter.save(member)

        assertThat(saved.id).isGreaterThan(0L)
        assertThat(saved.name).isEqualTo("홍길동")
        assertThat(saved.email).isEqualTo("hong@example.com")
    }

    @Test
    fun `존재하지 않는 이메일로 existsByEmail 호출하면 false를 반환한다`() {
        val result = memberAdapter.existsByEmail("notexist@example.com")

        assertThat(result).isFalse()
    }

    @Test
    fun `저장된 이메일로 existsByEmail 호출하면 true를 반환한다`() {
        val member = Member(name = "홍길동", email = "hong@example.com")
        memberAdapter.save(member)

        val result = memberAdapter.existsByEmail("hong@example.com")

        assertThat(result).isTrue()
    }
}
