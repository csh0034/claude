package com.ask.claude.application.member.port.`in`

import com.ask.claude.application.member.command.CreateMemberCommand
import com.ask.claude.application.member.result.CreateMemberResult

interface CreateMemberUseCase {
    fun createMember(command: CreateMemberCommand): CreateMemberResult
}
