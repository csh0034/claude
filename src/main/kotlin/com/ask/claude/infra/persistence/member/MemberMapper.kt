package com.ask.claude.infra.persistence.member

import com.ask.claude.domain.member.Member
import org.springframework.stereotype.Component

@Component
class MemberMapper {
    fun toDomain(entity: MemberJpaEntity): Member = Member(id = entity.id, name = entity.name, email = entity.email)

    fun toJpaEntity(domain: Member): MemberJpaEntity =
        MemberJpaEntity(id = domain.id, name = domain.name, email = domain.email)
}
