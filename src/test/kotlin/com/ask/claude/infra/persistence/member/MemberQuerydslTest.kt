package com.ask.claude.infra.persistence.member

import com.ask.claude.common.config.QuerydslConfig
import com.ask.claude.infra.persistence.member.QMemberJpaEntity.Companion.memberJpaEntity
import com.ask.claude.infra.persistence.member.fixture.MemberJpaFixture
import com.querydsl.jpa.impl.JPAQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(QuerydslConfig::class)
class MemberQuerydslTest {
    @Autowired
    private lateinit var jpaQueryFactory: JPAQueryFactory

    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @BeforeEach
    fun setUp() {
        memberJpaRepository.deleteAllInBatch()
    }

    @Test
    fun `QueryDSL로 전체 Member를 조회할 수 있다`() {
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "홍길동", email = "hong@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "김철수", email = "kim@example.com"))

        val members =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .fetch()

        assertThat(members).hasSize(2)
    }

    @Test
    fun `QueryDSL로 이름 조건으로 Member를 조회할 수 있다`() {
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "홍길동", email = "hong@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "김철수", email = "kim@example.com"))

        val result =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.name.eq("홍길동"))
                .fetchOne()

        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo("홍길동")
        assertThat(result.email).isEqualTo("hong@example.com")
    }

    @Test
    fun `QueryDSL로 이메일 조건으로 Member를 조회할 수 있다`() {
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "홍길동", email = "hong@example.com"))

        val result =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.email.eq("hong@example.com"))
                .fetchOne()

        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo("홍길동")
    }

    @Test
    fun `QueryDSL로 이름에 특정 문자열이 포함된 Member를 조회할 수 있다`() {
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "홍길동", email = "hong@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "홍길순", email = "hongsoon@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "김철수", email = "kim@example.com"))

        val results =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.name.startsWith("홍"))
                .fetch()

        assertThat(results).hasSize(2)
        assertThat(results).allSatisfy { assertThat(it.name).startsWith("홍") }
    }

    @Test
    fun `QueryDSL로 정렬하여 Member를 조회할 수 있다`() {
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "다", email = "da@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "가", email = "ga@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "나", email = "na@example.com"))

        val results =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .orderBy(memberJpaEntity.name.asc())
                .fetch()

        assertThat(results).hasSize(3)
        assertThat(results.map { it.name }).containsExactly("가", "나", "다")
    }

    @Test
    fun `QueryDSL로 페이징 조회할 수 있다`() {
        repeat(5) { i ->
            memberJpaRepository.save(
                MemberJpaFixture.createMemberJpaEntity(name = "회원$i", email = "member$i@example.com"),
            )
        }

        val results =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .orderBy(memberJpaEntity.id.asc())
                .offset(2)
                .limit(2)
                .fetch()

        assertThat(results).hasSize(2)
    }

    @Test
    fun `QueryDSL로 Member 수를 카운트할 수 있다`() {
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "홍길동", email = "hong@example.com"))
        memberJpaRepository.save(MemberJpaFixture.createMemberJpaEntity(name = "김철수", email = "kim@example.com"))

        val count =
            jpaQueryFactory
                .select(memberJpaEntity.count())
                .from(memberJpaEntity)
                .fetchOne()

        assertThat(count).isEqualTo(2L)
    }

    @Test
    fun `조건에 맞는 Member가 없으면 빈 리스트를 반환한다`() {
        val results =
            jpaQueryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.name.eq("존재하지않는이름"))
                .fetch()

        assertThat(results).isEmpty()
    }
}
