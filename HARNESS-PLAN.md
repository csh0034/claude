# Harness Engineering 적용 계획

## Context

이 프로젝트는 Kotlin Spring Boot Clean Architecture(Onion Architecture) 기반 애플리케이션으로, Claude Code AI 에이전트가 안정적으로 코드를 생성/수정할 수 있는 환경(Harness)을 구축하는 것이 목표이다.

**핵심 원칙**: `Model Quality < Harness Quality` — 같은 모델이라도 좋은 Harness가 있으면 안정적인 결과를, 없으면 데모 수준에 머문다.

**4대 핵심 구성요소**: Context Engineering, Architectural Constraints, Verification/Evaluation, Feedback Loop

---

## 1. Context Engineering (AI에게 적절한 정보를 제공)

> AI가 접근할 수 없는 정보는 존재하지 않는 것과 같다.

### 현재 상태

- `CLAUDE.md` — 프로젝트 개요, 기술 스택 정보 제공
- `.claude/rules/` — 4개 규칙 파일 운영 중
  - `architecture.md` — Onion Architecture 의존 방향, 패키지 import 규칙, 네이밍 컨벤션, Konsist 테스트 가이드
  - `testing.md` — 테스트 작성 규칙
  - `api-convention.md` — API 설계 규칙
  - `database.md` — 데이터베이스 관련 규칙
- `.claude/skills/` — 3개 스킬 운영 중
  - `commit/` — Angular Commit Convention 기반 커밋 자동화
  - `multi-review/` — 멀티 퍼소나 코드 리뷰
  - `redmine/` — Redmine 이슈 조회 및 구현

### 목표

- `CLAUDE.md` 보강 — 검증 명령어, 새 기능 추가 체크리스트 포함

### 작업 항목

- **1-1**: CLAUDE.md에 빌드/검증 명령어 섹션 추가 (`./gradlew check`, `./gradlew test`, `./gradlew ktlintFormat`, `./gradlew koverHtmlReport`) — 수정: `CLAUDE.md` ✅

---

## 2. Architectural Constraints (AI 행동 제한)

> AI의 행동을 제한하여 아키텍처 규칙 위반을 사전에 방지한다.

### 현재 상태

- **Konsist 아키텍처 테스트** (`src/test/kotlin/.../architecture/ArchitectureRuleTest.kt`)
  - 레이어 간 의존 방향 검증 (domain, application, infra, ui, common)
  - Domain 순수성 검증 (Spring/JPA/Jackson import 금지, @Entity/@Id/@Column 금지)
  - 네이밍 컨벤션 검증 (Port, UseCase, Service, Adapter, JpaEntity 등 위치 검사)
  - @Autowired 필드 주입 금지
- **protect-files.sh 훅** (PreToolUse: Edit, Write, Bash)
  - 보호 대상: `.env`, `README.md`, `.git/`

### 목표

- Konsist 규칙 보강 — 누락된 검증 항목 추가
- ktlint + detekt 정적 분석 도입 — 코드 품질 제약 자동화

### 작업 항목

| # | 작업 | 수정/신규 파일 |
|---|------|-------------|
| 2-1 | Konsist 규칙 보강: common 레이어 @Service/@Repository/@Controller 사용 금지, Domain Event가 Spring ApplicationEvent 미상속 검증, Query DTO가 application 패키지 위치 검증 | 수정: `src/test/kotlin/com/ask/claude/architecture/ArchitectureRuleTest.kt` |
| 2-2 | ktlint 플러그인 추가 (`org.jlleitschuh.gradle.ktlint`) | 수정: `build.gradle.kts`, 신규: `.editorconfig` | ✅ |
| 2-3 | detekt 플러그인 추가 — Kotlin 2.3.10과 호환되는 안정 버전 없음 (detekt 2.0 정식 출시 후 재검토) | 보류 | ⏸️ |

---

## 3. Verification / Evaluation (결과 자동 검증)

> AI가 만든 코드를 자동으로 검증하여 품질을 보장한다.

### 현재 상태

- **JUnit 5 + MockK** — 단위 테스트 프레임워크
- **Konsist** — 아키텍처 규칙 자동 검증 (테스트 시 실행)
- **Kover 0.9.7** — Kotlin 네이티브 커버리지 측정, 전체 70% 임계값 설정 ✅
- **통합 검증** — `./gradlew check`로 ktlint + 테스트 + koverVerify 자동 실행 (Gradle `check` 태스크에 자동 연결됨) ✅

### 목표

- ~~통합 `verify` 태스크~~ → 별도 태스크 불필요. ktlint/kover 플러그인이 `check` 태스크에 자동 연결됨
- Kover 커버리지 — 전체 70% 임계값 적용 완료. 패키지별 임계값(domain 90%, application 80%)은 향후 점진적으로 추가

### 작업 항목

| # | 작업 | 수정/신규 파일 | 상태 |
|---|------|-------------|------|
| 3-1 | ~~통합 verify 태스크~~ → 불필요. ktlint/kover가 Gradle `check` 태스크에 자동 의존 | — | ✅ (불필요) |
| 3-2 | Kover 플러그인 추가 (`org.jetbrains.kotlinx.kover:0.9.7`), 전체 커버리지 임계값 70% 설정. `./gradlew koverHtmlReport`로 리포트 생성 | 수정: `build.gradle.kts` | ✅ |

---

## 4. Feedback Loop (자동 수정 루프)

> AI가 스스로 문제를 감지하고 수정하는 반복 구조를 구축한다.

### 현재 상태

- **PreToolUse 훅만 존재** — `protect-files.sh`가 Edit/Write/Bash 전 파일 보호 검사
- PostToolUse 훅 없음 — 코드 변경 후 자동 검증 미실시
- 워크플로우 규칙 없음 — 변경 후 검증 실행이 규칙화되지 않음

### 목표

- PostToolUse 자동 테스트 훅 — `.kt` 파일 변경 시 자동 테스트 실행, 실패 시 오류 피드백
- 워크플로우 규칙 — 변경 후 검증 실행 필수화

### 작업 항목

| # | 작업 | 수정/신규 파일 |
|---|------|-------------|
| 4-1 | PostToolUse 자동 테스트 훅: Edit/Write 후 `.kt` 파일 변경 시 `./gradlew test --fail-fast --quiet` 실행, 실패 시 오류 로그 전달 | 신규: `.claude/hooks/post-edit-verify.sh`, 수정: `.claude/settings.json` |
| 4-2 | 워크플로우 규칙: 기능 완료 후 `./gradlew check` 실행 필수, 멀티 스텝 작업 시 레이어별 중간 커밋 | 신규: `.claude/rules/workflow.md` |

---

## 5. Multi-Agent 협업 (에이전트 간 협업 워크플로우)

> 복수 에이전트가 역할을 분담하여 코드 생성, 리뷰, 검증을 수행한다.

### 현재 상태

- `multi-review/` 스킬 — 여러 전문가 관점에서 코드 리뷰 수행
- `commit/` 스킬 — Angular Commit Convention 기반 커밋 자동화
- `redmine/` 스킬 — Redmine 이슈 연동

### 목표

- 검증 스킬 — `/validate` 명령으로 전체 검증 + 결과 리포트

### 작업 항목

| # | 작업 | 수정/신규 파일 |
|---|------|-------------|
| 5-1 | 검증 스킬: `./gradlew check` 실행 후 결과를 구조화하여 리포트, 실패 항목별 수정 가이드 제공 | 신규: `.claude/skills/validate/SKILL.md` |

---

## 실행 우선순위 및 의존성

```
[2-2] ktlint ✅   [2-3] detekt ⏸️ (Kotlin 2.3.10 호환 대기)

[3-1] check 태스크 활용 ✅ ──→ [4-1] PostToolUse 훅
[3-2] Kover 커버리지 ✅          │
[1-1] CLAUDE.md 보강 ✅          ▼
                           [4-2] 워크플로우 규칙

[2-1] Konsist 보강 ─────── 독립, 언제든 가능
[5-1] 검증 스킬 ─────────→ check 태스크 활용
```

**남은 작업 순서**:
1. 4-1 → 4-2 (피드백 루프 → 워크플로우)
2. 2-1 (독립 작업)
3. 5-1 (검증 스킬)
4. 2-3 (detekt 2.0 정식 출시 후)

---

## 검증 방법

- HARNESS.md의 4대 핵심 구성요소(Context Engineering, Constraints, Verification, Feedback Loop)가 빠짐없이 커버되는지 확인
- 모든 작업 항목에 수정/신규 파일 경로가 명시되어 있는지 확인
- 각 Phase 완료 후 `./gradlew check` 실행하여 전체 검증 성공 확인
- 의도적으로 잘못된 코드 작성 후 ktlint/Konsist가 잡아내는지 확인
- `.kt` 파일 수정 후 PostToolUse 훅이 자동 테스트를 트리거하는지 확인

---

## 참고 자료

- [OpenAI - Harness Engineering](https://openai.com/index/harness-engineering/)
- [Anthropic - Effective Harnesses for Long-Running Agents](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [Martin Fowler - Harness Engineering](https://martinfowler.com/articles/exploring-gen-ai/harness-engineering.html)
- [Phil Schmid - The Importance of Agent Harness in 2026](https://www.philschmid.de/agent-harness-2026)
