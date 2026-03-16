package com.ask.claude.infra.persistence.member.fixture

import com.ask.claude.infra.persistence.member.MemberJpaEntity

object MemberJpaFixture {
    fun createMemberJpaEntity(
        id: Long = 0L,
        name: String = "홍길동",
        email: String = "hong@example.com",
    ): MemberJpaEntity = MemberJpaEntity(id = id, name = name, email = email)
}
