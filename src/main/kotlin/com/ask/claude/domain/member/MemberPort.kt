package com.ask.claude.domain.member

interface MemberPort {
    fun save(member: Member): Member
    fun existsByEmail(email: String): Boolean
}
