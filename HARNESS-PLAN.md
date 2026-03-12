# Harness Engineering 적용 계획

## Context

이 프로젝트는 Kotlin Spring Boot Clean Architecture 기반 애플리케이션으로, Claude Code 같은 AI 에이전트가 안정적으로 코드를 생성/수정할 수 있는 환경(Harness)을 구축하는 것이 목표이다.

**Harness Engineering 핵심 원칙**: `Model Quality < Harness Quality` — 같은 모델이라도 좋은 Harness가 있으면 안정적인 결과를 만들고, 없으면 데모 수준에 머문다.

**현재 상태**: Context Engineering(CLAUDE.md, architecture rules)과 Architectural Constraints(ArchUnit 테스트, 레이어별 테스트)가 이미 잘 갖춰져 있다. **Verification 자동화**와 **Feedback Loop**이 주요 갭이다.

---

## Phase 1: Verification 인프라 구축 (최고 우선순위)

AI가 만든 코드를 자동으로 검증하는 체계를 구축한다.

### 1.1 ktlint + detekt 정적 분석 추가

**수정 파일**: `build.gradle.kts`

- `org.jlleitschuh.gradle.ktlint` 플러그인 추가 (코드 포맷 강제)
- `io.gitlab.arturbosch.detekt` 플러그인 추가 (정적 분석)

**신규 파일**:
- `.editorconfig` — ktlint 설정 (`ktlint_code_style = ktlint_official`)
- `config/detekt/detekt.yml` — detekt 규칙 설정 (복잡도 임계값, 프로젝트에 맞는 룰 조정)

### 1.2 통합 검증 태스크

**수정 파일**: `build.gradle.kts`

```kotlin
tasks.register("verify") {
    dependsOn("ktlintCheck", "detektMain", "test")
    description = "전체 검증: 포맷 + 정적분석 + 테스트"
}
```

AI 에이전트가 `./gradlew verify` 한 줄로 모든 검증을 실행할 수 있게 한다.

### 1.3 JaCoCo 코드 커버리지

**수정 파일**: `build.gradle.kts`

- `jacoco` 플러그인 추가
- 커버리지 임계값: domain 90%, application 80%, 전체 70%
- `verify` 태스크에 커버리지 검증 연결

### 1.4 GitHub Actions CI

**신규 파일**: `.github/workflows/ci.yml`

- push/PR 트리거
- Java 25 + Gradle wrapper 설정
- `./gradlew verify` 실행
- Gradle 캐싱 + 테스트 리포트 업로드

---

## Phase 2: Feedback Loop 구축 (높은 우선순위)

Phase 1의 `verify` 태스크를 활용하여 AI의 자기 수정 루프를 만든다.

### 2.1 PostToolUse 자동 테스트 훅

**신규 파일**: `.claude/hooks/post-edit-verify.sh`

Edit/Write 도구 사용 후 `.kt` 파일이 변경되면 자동으로 `./gradlew test --fail-fast --quiet` 실행. 실패 시 오류 로그를 Claude에게 전달하여 자동 수정 유도.

**수정 파일**: `.claude/settings.json` — PostToolUse 훅 등록

### 2.2 파일 보호 훅 강화

**수정 파일**: `.claude/hooks/protect-files.sh`

현재 `.env`, `README.md`, `.git/`만 보호 중. 추가 보호 대상:
- `build.gradle.kts`, `settings.gradle.kts` (경고만, 차단하지 않음)
- `.claude/settings.json`, `.claude/hooks/` (AI가 자기 제약을 완화하는 것 방지)
- `gradlew`, `gradlew.bat`

### 2.3 워크플로우 규칙

**신규 파일**: `.claude/rules/workflow.md`

- 기능 완료 후 커밋 메시지에 변경 요약 포함
- 멀티 스텝 작업 시 레이어별 중간 커밋
- 변경 후 `./gradlew verify` 실행 필수

---

## Phase 3: Context Engineering 강화 (중간 우선순위)

AI가 프로젝트를 빠르게 이해할 수 있는 맥락 정보를 보강한다.

### 3.1 ARCHITECTURE.md 생성

**신규 파일**: `ARCHITECTURE.md`

- ASCII 레이어 다이어그램
- 의존 방향 시각화
- 패키지 구조 트리
- 데이터 흐름 예시 (Create Member 전체 흐름)
- 새 기능 추가 체크리스트

### 3.2 ADR (Architecture Decision Records)

**신규 디렉토리**: `docs/adr/`

- `0001-clean-architecture-onion.md` — Onion Architecture 선택 이유
- `0002-domain-jpa-entity-separation.md` — Domain Entity ≠ JPA Entity 결정
- `0003-output-port-in-domain.md` — Output Port가 domain에 위치하는 이유
- `template.md` — 향후 ADR 템플릿

### 3.3 CLAUDE.md 보강

**수정 파일**: `CLAUDE.md`

검증 명령어 섹션 추가:
```
## 빌드 및 검증 명령
- ./gradlew verify — 전체 검증
- ./gradlew test — 테스트만
- ./gradlew ktlintFormat — 포맷 자동 수정
```

---

## Phase 4: Architectural Constraints 확장 (중간 우선순위)

### 4.1 ArchUnit 규칙 보강

**수정 파일**: `src/test/kotlin/com/ask/claude/architecture/ArchitectureRuleTest.kt`

누락된 규칙 추가:
- common 레이어 `@Service`/`@Repository`/`@Controller` 사용 금지 (`@Configuration`만 허용)
- Domain Event가 Spring `ApplicationEvent`를 상속하지 않는 것 검증
- `Query` DTO가 application 패키지에 위치하는 것 검증

### 4.2 스캐폴딩 스킬

**신규 파일**: `.claude/skills/scaffold/SKILL.md`

새 도메인 기능 추가 시 Clean Architecture 전 레이어를 TDD 순서로 생성하는 구조화된 절차 제공.

---

## Phase 5: 통합 테스트 & 관측성 (낮은 우선순위)

### 5.1 API 통합 테스트

**신규 파일**: `src/test/kotlin/com/ask/claude/integration/MemberApiIntegrationTest.kt`

`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`으로 HTTP 요청부터 DB까지 전체 흐름 검증.

### 5.2 Actuator 헬스 체크

**수정 파일**: `build.gradle.kts` — `spring-boot-starter-actuator` 의존성 추가
**수정 파일**: `src/main/resources/application.yaml` — health, info 엔드포인트 노출

### 5.3 로깅 규칙

**신규 파일**: `.claude/rules/logging.md` — 로깅 레벨 규칙, 민감 데이터 로깅 금지 등

---

## Phase 6: Git 훅 & 고급 피드백 (향후)

### 6.1 Pre-commit Git 훅

**신규 파일**: `.githooks/pre-commit` — 커밋 전 `ktlintCheck` + `detektMain` 자동 실행
**수정 파일**: `build.gradle.kts` — `installGitHooks` 태스크 추가

### 6.2 검증 스킬

**신규 파일**: `.claude/skills/validate/SKILL.md` — `/validate` 명령으로 전체 검증 + 리포트

---

## 실행 순서 및 의존성

```
Phase 1 ──→ Phase 2 (verify 태스크 필요)
  │
  ├──→ Phase 5 (빌드 설정 필요)
  │
Phase 3 (독립, Phase 1과 병렬 가능)
Phase 4 (독립, 언제든 가능)
Phase 6 (Phase 1-2 완료 후)
```

## 검증 방법

각 Phase 완료 후:
1. `./gradlew verify` 실행하여 전체 빌드 성공 확인
2. 의도적으로 잘못된 코드 작성 후 ktlint/detekt/ArchUnit이 잡아내는지 확인
3. `.kt` 파일 수정 후 PostToolUse 훅이 자동 테스트를 트리거하는지 확인
4. CI 파이프라인에서 PR의 검증이 자동 실행되는지 확인

## 참고 자료

- [OpenAI - Harness Engineering](https://openai.com/index/harness-engineering/)
- [Anthropic - Effective Harnesses for Long-Running Agents](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [Martin Fowler - Harness Engineering](https://martinfowler.com/articles/exploring-gen-ai/harness-engineering.html)
- [Phil Schmid - The Importance of Agent Harness in 2026](https://www.philschmid.de/agent-harness-2026)

---

## 추가 참고 자료 (csh0034)

- [아키텍처 의사결정 기록 (Architecture Decision Record, ADR) 무엇인가?](https://www.cncf.co.kr/blog/adr-guide/)
- [ADR을 써야 하는 이유](https://news.hada.io/topic?id=2665)
