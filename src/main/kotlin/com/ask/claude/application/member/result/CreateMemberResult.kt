package com.ask.claude.application.member.result

import com.ask.claude.domain.member.Member

data class CreateMemberResult(
    val id: Long,
    val name: String,
    val email: String,
) {
    companion object {
        fun from(member: Member): CreateMemberResult = CreateMemberResult(
            id = member.id,
            name = member.name,
            email = member.email,
        )
    }
}
