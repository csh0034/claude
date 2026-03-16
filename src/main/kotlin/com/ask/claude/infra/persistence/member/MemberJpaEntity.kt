package com.ask.claude.infra.persistence.member

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "members",
    indexes = [Index(name = "idx_member_email", columnList = "email", unique = true)],
)
class MemberJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false, length = 100)
    val name: String,
    @Column(nullable = false, unique = true, length = 254)
    val email: String,
)
