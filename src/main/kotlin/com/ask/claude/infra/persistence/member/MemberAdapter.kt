package com.ask.claude.infra.persistence.member

import com.ask.claude.domain.member.Member
import com.ask.claude.domain.member.MemberPort
import org.springframework.stereotype.Repository

@Repository
class MemberAdapter(
    private val memberJpaRepository: MemberJpaRepository,
    private val memberMapper: MemberMapper,
) : MemberPort {
    override fun save(member: Member): Member {
        val entity = memberMapper.toJpaEntity(member)
        val saved = memberJpaRepository.save(entity)
        return memberMapper.toDomain(saved)
    }

    override fun existsByEmail(email: String): Boolean = memberJpaRepository.existsByEmail(email)
}
