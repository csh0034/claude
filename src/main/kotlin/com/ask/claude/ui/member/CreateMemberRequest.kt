package com.ask.claude.ui.member

import com.ask.claude.application.member.command.CreateMemberCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateMemberRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 100, message = "이름은 100자 이하여야 합니다")
    val name: String = "",
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "이메일 형식이 올바르지 않습니다")
    @field:Size(max = 254, message = "이메일은 254자 이하여야 합니다")
    val email: String = "",
) {
    fun toCommand(): CreateMemberCommand = CreateMemberCommand(name = name, email = email)
}
