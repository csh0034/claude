package com.ask.claude.application.member.service

import com.ask.claude.application.member.command.CreateMemberCommand
import com.ask.claude.application.member.port.`in`.CreateMemberUseCase
import com.ask.claude.application.member.result.CreateMemberResult
import com.ask.claude.common.exception.BusinessException
import com.ask.claude.common.exception.ErrorCode
import com.ask.claude.domain.member.Member
import com.ask.claude.domain.member.MemberPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateMemberService(
    private val memberPort: MemberPort,
) : CreateMemberUseCase {

    @Transactional
    override fun createMember(command: CreateMemberCommand): CreateMemberResult {
        val email = command.email.lowercase()
        if (memberPort.existsByEmail(email)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }
        val member = Member(name = command.name, email = email)
        val saved = memberPort.save(member)
        return CreateMemberResult.from(saved)
    }
}
