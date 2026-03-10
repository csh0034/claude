package com.ask.claude.application.member.command

data class CreateMemberCommand(
    val name: String,
    val email: String,
)
