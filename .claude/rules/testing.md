---
paths:
  - src/test/kotlin/**/*
  - src/main/kotlin/**/application/**/service/**/*
  - src/main/kotlin/**/domain/**/*
---

# 테스트 작성 규칙 (TDD + 클린 아키텍처)

## TDD 사이클

IMPORTANT: 새로운 기능 구현 및 버그 수정 시 반드시 TDD 사이클을 따른다.

1. **Red** — 실패하는 테스트를 먼저 작성한다. 컴파일 에러도 실패로 간주한다
2. **Green** — 테스트를 통과시키는 최소한의 코드만 작성한다. 과도한 설계 금지
3. **Refactor** — 테스트가 통과하는 상태를 유지하면서 코드를 개선한다

- IMPORTANT: 프로덕션 코드보다 테스트 코드를 먼저 작성한다
- IMPORTANT: 한 번에 하나의 테스트만 추가하고, 통과시킨 후 다음 테스트로 진행한다
- Refactor 단계에서 테스트가 깨지면 즉시 되돌리고 더 작은 단위로 리팩터링한다

## 레이어별 테스트 작성 순서

IMPORTANT: 새 기능 구현 시 반드시 안쪽 레이어부터 바깥쪽 레이어 순서로 작성한다.

**① domain → ② application → ③ infra → ④ ui**

각 레이어에서 TDD 사이클(Red-Green-Refactor)을 완료한 후 다음 레이어로 진행한다.

| 순서 | 패키지 | 테스트 종류 | 도구 | 대상 |
|------|--------|-----------|------|------|
| ① | domain | 순수 단위 테스트 | JUnit 5만 | Entity 비즈니스 로직, VO, Domain Service |
| ② | application | 단위 테스트 | MockK | UseCase (Port를 Mock) |
| ③ | infra | 통합 테스트 | @DataJpaTest | Adapter, Mapper, JPA |
| ④ | ui | 슬라이스 테스트 | @WebMvcTest + MockK | Controller, 요청/응답 검증 |

## 테스트 네이밍

백틱(`) 사용한 한글 메서드명: `fun \`회원 생성 시 이름이 빈 값이면 예외가 발생한다\`()`

## 레이어별 테스트 전략

### ① Domain 테스트 — 순수 단위 테스트 (가장 먼저 작성)

- Spring 의존성 없이 순수 Kotlin 테스트
- Mock 사용 금지 — 실제 도메인 객체만 사용
- Entity 생성, 상태 변경, 비즈니스 규칙 검증에 집중
- Domain Event 발행 로직 검증 포함

### ② Application 테스트 — UseCase 단위 테스트

- Output Port(Repository 등) 인터페이스를 MockK로 모킹
- Adapter 구현체는 참조하지 않음 (infra 테스트에서 별도 수행)
- Command/Query 입력 → Result 출력 흐름 검증

### ③ Infra 테스트 — Adapter 통합 테스트

- `@DataJpaTest`로 JPA 슬라이스 테스트
- Mapper의 Domain ↔ JPA 변환 정합성 검증
- Adapter가 domain Output Port 계약을 올바르게 구현하는지 확인

### ④ UI 테스트 — Controller 슬라이스 테스트

- `@WebMvcTest` + `@MockkBean`으로 UseCase(Input Port) 모킹
- HTTP 상태 코드, 요청/응답 JSON 형식 검증
- Request → Command 변환, Result → Response 변환 검증
- Validation 어노테이션 동작 검증 포함

## Fixture 규칙

- IMPORTANT: 테스트 데이터 생성은 Fixture 함수로 통일한다
- Fixture 함수는 `src/test/kotlin/.../fixture/` 패키지에 위치한다
- 레이어별 Fixture 분리: `DomainFixture`(Domain Entity/VO), `ApplicationFixture`(Command/Query/Result DTO), `JpaFixture`(JPA Entity)
- Fixture 함수는 named parameter + default value 패턴을 사용하여 테스트마다 필요한 값만 오버라이드한다
- Fixture 함수명: `create{ClassName}` (예: `createMember()`, `createCreateMemberCommand()`, `createMemberResult()`, `createMemberJpaEntity()`)

## 테스트 원칙

- IMPORTANT: given/when/then 패턴 필수
- IMPORTANT: MockK 사용 (Mockito 아님)
- IMPORTANT: 비즈니스 로직 변경 시 반드시 테스트 작성/수정
- 테스트 간 독립성 보장 (순서 의존 금지)
- 테스트 클래스명: `{대상클래스}Test` (예: `MemberTest`, `CreateMemberServiceTest`)
- SUT(System Under Test)는 `sut` 변수명 사용
