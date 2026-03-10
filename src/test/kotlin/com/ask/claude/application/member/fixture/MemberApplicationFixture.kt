package com.ask.claude.application.member.fixture

import com.ask.claude.application.member.command.CreateMemberCommand
import com.ask.claude.application.member.result.CreateMemberResult

object MemberApplicationFixture {

    fun createMemberCommand(
        name: String = "홍길동",
        email: String = "hong@example.com",
    ): CreateMemberCommand = CreateMemberCommand(name = name, email = email)

    fun createMemberResult(
        id: Long = 1L,
        name: String = "홍길동",
        email: String = "hong@example.com",
    ): CreateMemberResult = CreateMemberResult(id = id, name = name, email = email)
}
