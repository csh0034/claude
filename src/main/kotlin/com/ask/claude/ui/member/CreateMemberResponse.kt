package com.ask.claude.ui.member

import com.ask.claude.application.member.result.CreateMemberResult

data class CreateMemberResponse(
    val id: Long,
    val name: String,
    val email: String,
) {
    companion object {
        fun from(result: CreateMemberResult): CreateMemberResponse =
            CreateMemberResponse(
                id = result.id,
                name = result.name,
                email = result.email,
            )
    }
}
