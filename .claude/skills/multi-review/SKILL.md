---
name: multi-review
description: Security, Performance, Maintainability, Architecture, Testing 5개 관점에서 병렬 코드 리뷰를 수행한다
argument-hint: all | changes (default)
disable-model-invocation: true
allowed-tools: Bash(git *), Glob, Grep, Read, Agent
---

# 멀티 퍼소나 코드 리뷰

5개 전문가 관점(Security, Performance, Maintainability, Architecture, Testing)에서 병렬로 코드 리뷰를 수행하고, 심각도별로 종합 리포트를 생성한다.

## 1. 리뷰 대상 결정

인자에 따라 리뷰 대상을 결정한다.

1. **`all`**: 프로젝트의 모든 소스 파일(`src/` 하위)을 리뷰 대상으로 한다.
2. **`changes`** (default, 인자 없는 경우 포함): 아래 순서로 변경사항을 탐색한다.
    - `git diff --staged -- src/` → staged 변경사항 중 `src/` 하위 파일만 사용
    - `git diff -- src/` → unstaged 변경사항 중 `src/` 하위 파일만 사용
    - 둘 다 없으면 사용자에게 "리뷰할 변경사항이 없습니다"라고 안내하고 종료

수집한 파일 목록과 내용(또는 diff)을 각 Agent에 전달한다.

## 2. Agent 병렬 실행

5개의 Agent를 **반드시 병렬로** 실행한다 (단일 메시지에 5개 Agent 호출).
각 Agent에는 아래 정보를 포함한 프롬프트를 전달한다:

- 변경된 파일 목록
- diff 전문 (또는 파일 전체 내용)
- 해당 관점의 체크리스트
- 프로젝트 규칙 파일 경로 (필요 시 Read하도록 지시)

모든 Agent의 프롬프트에 공통으로 포함할 내용:

- "리뷰 결과만 보고하라. 코드를 수정하지 마라."
- "지적 사항이 없으면 '이상 없음'으로 보고하라."
- "각 지적에 심각도(Critical/Major/Minor/Suggestion), 파일:라인, 설명, 개선 방안을 포함하라."

---

### 2-1. Security Agent

보안 전문가 관점에서 취약점을 검토한다.

**체크리스트:**

- SQL Injection: 문자열 결합으로 쿼리를 생성하는지, parameterized query / named parameter 사용 여부
- XSS: 사용자 입력이 응답에 그대로 반영되는지, 이스케이프 처리 여부
- 인증/인가: 엔드포인트에 적절한 인증 체크가 있는지, 권한 검증 누락 여부
- 민감정보 노출: 로그에 패스워드/토큰/개인정보가 포함되는지, 응답에 불필요한 내부 정보 노출
- CSRF/CORS: 설정이 적절한지
- 의존성: 알려진 취약점이 있는 라이브러리 사용 여부
- 입력 검증: Controller의 `@Valid` / Bean Validation 누락, 경계값 미처리
- 하드코딩된 시크릿: API 키, 비밀번호 등이 소스에 포함되었는지

---

### 2-2. Performance Agent

성능 전문가 관점에서 병목과 비효율을 검토한다.

**체크리스트:**

- N+1 쿼리: 연관관계 로딩 시 LAZY fetch로 인한 N+1 발생 가능성, `@EntityGraph` 또는 fetch join 필요 여부
- 불필요한 쿼리: 같은 데이터를 반복 조회하는지, 배치 처리가 가능한데 건별 처리하는지
- 메모리 누수: 대용량 컬렉션을 메모리에 전부 적재하는지, Stream/Sequence 미사용
- 블로킹 호출: I/O 작업이 메인 스레드를 블로킹하는지
- 인덱스: 쿼리 조건에 인덱스가 필요한 컬럼이 있는지
- 캐싱: 반복 호출되는 불변 데이터에 캐시 적용 여부
- 페이징: 대량 데이터 조회 시 페이징 미적용

---

### 2-3. Maintainability Agent

유지보수성 전문가 관점에서 코드 품질을 검토한다.

**체크리스트:**

- 복잡도: 메서드가 과도하게 길거나 분기가 깊은지 (cyclomatic complexity)
- 중복 코드: 유사한 로직이 반복되는지
- 네이밍: 변수/함수/클래스명이 의도를 명확히 전달하는지
- 단일 책임: 하나의 클래스/메서드가 너무 많은 책임을 지는지
- 매직 넘버/문자열: 상수로 추출해야 할 리터럴이 있는지
- 에러 처리: 예외를 삼키거나(catch 후 무시), 너무 넓은 Exception을 catch하는지
- 가독성: 불필요하게 복잡한 표현, Kotlin 관용구 미활용

---

### 2-4. Architecture Agent

아키텍처 전문가 관점에서 클린 아키텍처 규칙 준수를 검토한다.

**프롬프트에 반드시 포함:**

- `.claude/rules/architecture.md` 파일을 Read하여 규칙을 숙지할 것

**체크리스트:**

- 의존 방향: `ui → application → domain ← infra` 방향 위반 여부
- import 규칙: `architecture.md`의 패키지 import 허용/금지 테이블 기준으로 역방향 import 검출
- Domain 순수성: Domain 패키지에 Spring/JPA 어노테이션이 포함되었는지
- Port/Adapter 패턴: Output Port가 domain에 interface로 정의되고 infra의 Adapter가 구현하는지
- UseCase 단일 책임: 하나의 UseCase가 하나의 기능만 수행하는지
- DTO 분리: Application DTO(Command/Result)와 UI DTO(Request/Response)가 별개 클래스인지
- 네이밍 컨벤션: `architecture.md`의 네이밍 테이블 준수 여부 (Port, UseCase, Service, Adapter, JpaEntity, JpaRepository 등)
- Controller 의존: Controller가 UseCase(Input Port)만 의존하고 Service 구현체를 직접 참조하지 않는지

---

### 2-5. Testing Agent

테스트 전문가 관점에서 테스트 품질과 커버리지를 검토한다.

**프롬프트에 반드시 포함:**

- `.claude/rules/testing.md` 파일을 Read하여 규칙을 숙지할 것
- 변경된 프로덕션 코드에 대응하는 테스트 파일이 존재하는지 확인할 것

**체크리스트:**

- 테스트 존재 여부: 변경된 비즈니스 로직에 대응하는 테스트가 있는지
- 레이어별 테스트 전략: Domain(순수 단위), Application(MockK), Infra(@DataJpaTest), UI(@WebMvcTest) 패턴 준수
- given/when/then 패턴: 테스트 구조가 패턴을 따르는지
- MockK 사용: Mockito가 아닌 MockK를 사용하는지
- Fixture 사용: 테스트 데이터 생성에 Fixture 함수를 사용하는지, `create{ClassName}` 네이밍 준수
- 테스트 네이밍: 백틱 한글 메서드명 사용 여부
- SUT 변수명: `sut` 변수명 사용 여부
- 테스트 독립성: 테스트 간 상태 공유나 순서 의존이 없는지

## 3. 결과 종합

모든 Agent의 결과를 수집한 후 아래 규칙으로 종합 리포트를 작성한다.

### 종합 규칙

- 심각도 순서대로 정렬: Critical > Major > Minor > Suggestion
- 여러 Agent가 같은 위치에서 같은 문제를 지적한 경우 하나로 병합하고, 관련 관점을 표기
- 지적이 전혀 없으면 "모든 관점에서 이상 없음"으로 보고

### 출력 형식

```
## 리뷰 요약
- 리뷰 대상: {파일 목록 또는 "staged 변경사항" 등}
- 총 지적 사항: Critical {n}건, Major {n}건, Minor {n}건, Suggestion {n}건

### 🔴 Critical
- [{관점}] `파일경로:라인` — 설명
  → 개선 방안

### 🟠 Major
- [{관점}] `파일경로:라인` — 설명
  → 개선 방안

### 🟡 Minor
- [{관점}] `파일경로:라인` — 설명
  → 개선 방안

### 💡 Suggestion
- [{관점}] `파일경로:라인` — 설명
  → 개선 방안
```

심각도 카테고리에 해당 항목이 없으면 해당 섹션은 생략한다.
