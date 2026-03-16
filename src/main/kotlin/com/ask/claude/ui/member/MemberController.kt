package com.ask.claude.ui.member

import com.ask.claude.application.member.port.CreateMemberUseCase
import com.ask.claude.ui.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/members")
class MemberController(
    private val createMemberUseCase: CreateMemberUseCase,
) {
    @PostMapping
    fun createMember(
        @Valid @RequestBody request: CreateMemberRequest,
    ): ResponseEntity<ApiResponse<CreateMemberResponse>> {
        val command = request.toCommand()
        val result = createMemberUseCase.createMember(command)
        val response = CreateMemberResponse.from(result)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))
    }
}
