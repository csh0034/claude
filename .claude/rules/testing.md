---
paths:
  - src/test/kotlin/**/*
  - src/main/kotlin/**/application/**/service/**/*
  - src/main/kotlin/**/domain/**/*
---

# 테스트 작성 규칙 (클린 아키텍처)

## 레이어별 테스트 전략

| 패키지 | 테스트 종류 | 도구 | 대상 |
|--------|-----------|------|------|
| domain | 순수 단위 테스트 | JUnit 5만 | Entity 비즈니스 로직, VO, Domain Service |
| application | 단위 테스트 | MockK | UseCase (Port를 Mock) |
| infra | 통합 테스트 | @DataJpaTest, Testcontainers | Adapter, Mapper, JPA |
| ui | 슬라이스 테스트 | @WebMvcTest + MockK | Controller, 요청/응답 검증 |
| 전체 | E2E 통합 테스트 | @SpringBootTest | 전체 플로우 |

## 테스트 네이밍

백틱(`) 사용한 한글 메서드명:

```kotlin
@Test
fun `회원 생성 시 이름이 빈 값이면 예외가 발생한다`() { ... }
```

## Domain 테스트 — 순수 단위 테스트

```kotlin
class MemberTest {
    @Test
    fun `활성 회원을 비활성화한다`() {
        // given
        val member = Member(id = 1L, name = "홍길동", email = "hong@test.com")

        // when
        member.deactivate()

        // then
        assertThat(member.status).isEqualTo(MemberStatus.INACTIVE)
    }

    @Test
    fun `이미 비활성 회원을 비활성화하면 예외가 발생한다`() {
        // given
        val member = Member(id = 1L, name = "홍길동", email = "hong@test.com",
            status = MemberStatus.INACTIVE)

        // when & then
        assertThatThrownBy { member.deactivate() }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
```

- Spring 의존성 없이 순수 Kotlin 테스트
- Domain Entity의 비즈니스 로직을 직접 테스트

## Application 테스트 — UseCase 단위 테스트

```kotlin
class CreateMemberServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val eventPublisher = mockk<EventPublisher>(relaxed = true)
    private val sut = CreateMemberService(memberRepository, eventPublisher)

    @Test
    fun `회원을 생성한다`() {
        // given
        val command = CreateMemberCommand(name = "홍길동", email = "hong@test.com")
        every { memberRepository.existsByEmail(any()) } returns false
        every { memberRepository.save(any()) } returns Member(
            id = 1L, name = "홍길동", email = "hong@test.com"
        )

        // when
        val result = sut.execute(command)

        // then
        assertThat(result.name).isEqualTo("홍길동")
        verify(exactly = 1) { memberRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publish(any()) }
    }
}
```

- Port 인터페이스를 MockK로 모킹
- Adapter 구현체는 테스트하지 않음 (infra 테스트에서 별도 수행)

## Infra 테스트 — Adapter 통합 테스트

```kotlin
@DataJpaTest
class MemberRepositoryAdapterTest {
    @Autowired
    private lateinit var jpaRepository: MemberJpaRepository
    private lateinit var sut: MemberRepositoryAdapter

    @BeforeEach
    fun setUp() {
        sut = MemberRepositoryAdapter(jpaRepository, MemberMapper())
    }

    @Test
    fun `Domain Entity를 저장하고 조회한다`() {
        // given
        val member = Member(name = "홍길동", email = "hong@test.com")

        // when
        val saved = sut.save(member)
        val found = sut.findById(saved.id!!)

        // then
        assertThat(found).isNotNull
        assertThat(found!!.name).isEqualTo("홍길동")
    }
}
```

- Mapper의 Domain ↔ JPA 변환이 올바른지 검증
- 실제 DB 연동 테스트 (H2 또는 Testcontainers)

## UI 테스트 — Controller 슬라이스 테스트

```kotlin
@WebMvcTest(MemberController::class)
class MemberControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createMemberUseCase: CreateMemberUseCase

    @Test
    fun `회원 생성 API`() {
        // given
        val request = MemberCreateRequest(name = "홍길동", email = "hong@test.com")
        every { createMemberUseCase.execute(any()) } returns MemberResult(
            id = 1L, name = "홍길동", email = "hong@test.com"
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.name").value("홍길동"))
    }
}
```

- UseCase(Input Port) 인터페이스를 MockK로 모킹
- HTTP 상태 코드, 응답 JSON 형식 검증

## 테스트 원칙

- IMPORTANT: given/when/then 패턴 필수
- MockK 사용 (Mockito 아님)
- Domain 테스트에는 Mock 사용하지 않음 (순수 로직 테스트)
- 비즈니스 로직 변경 시 반드시 테스트 작성/수정
- 테스트 간 독립성 보장 (순서 의존 금지)
