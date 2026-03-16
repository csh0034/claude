package com.ask.claude.domain.member.fixture

import com.ask.claude.domain.member.Member

object MemberDomainFixture {
    fun unsavedMember(
        name: String = "홍길동",
        email: String = "hong@example.com",
    ): Member = Member(id = 0L, name = name, email = email)

    fun savedMember(
        id: Long = 1L,
        name: String = "홍길동",
        email: String = "hong@example.com",
    ): Member = Member(id = id, name = name, email = email)

    fun createMember(
        id: Long = 1L,
        name: String = "홍길동",
        email: String = "hong@example.com",
    ): Member = savedMember(id = id, name = name, email = email)
}
