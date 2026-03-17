# AI 생태계에서의 Harness Engineering

> 소프트웨어 엔지니어링 팀의 주된 업무는 더 이상 코드를 작성하는 것이 아니라, 환경을 설계하고, 의도를 명시하며, 에이전트가 신뢰할 수 있는 작업을 수행할 수 있는 피드백 루프를 구축하는 것

## 1. 개요

**Harness Engineering**은 AI 모델(LLM, AI Agent)을 실제 서비스나 시스템에서 안정적으로 작동하도록 만드는 **제어 시스템과 실행 환경을 설계하는 엔지니어링 분야**이다.

AI 모델 자체는 단순히 **추론 엔진**에 가깝기 때문에 실제 운영 환경에서는 다음과 같은 문제가 발생한다.

* 잘못된 행동 수행
* 불안정한 결과
* 보안 문제
* 잘못된 데이터 접근
* 재현 불가능한 결과

Harness Engineering은 이러한 문제를 해결하기 위해 **AI 주변에 제어 계층(control layer)**을 구축한다.

비유:

```
AI 모델 = 엔진
Harness = 차체 + 브레이크 + 변속기 + 제어 시스템
```

즉 **AI의 능력을 안전하게 활용하도록 감싸는 시스템**이다.

---

# 2. Harness Engineering 등장 배경

최근 AI 개발 패러다임은 다음과 같이 변화하고 있다.

과거 개발 방식

```
Human -> Code 작성 -> Program 실행
```

AI Agent 기반 개발

```
Human -> 환경 설계
AI Agent -> 코드 생성 / 작업 수행
```

즉 개발자의 역할이

```
코드 작성 → AI가 일할 환경 설계
```

로 이동하고 있다.

이 환경 설계를 담당하는 영역이 **Harness Engineering**이다.

---

# 3. Harness Engineering의 핵심 구성 요소

AI Harness는 보통 다음과 같은 계층으로 구성된다.

## 3.1 Context Engineering

AI에게 **적절한 정보를 제공하는 시스템**

예시

* 코드베이스
* 설계 문서
* API 스펙
* 프로젝트 규칙
* 로그 및 메트릭

대표적인 방식

* RAG (Retrieval-Augmented Generation)
* Repository 문서
* `AGENTS.md`
* `ARCHITECTURE.md`

핵심 원칙

> AI가 접근할 수 없는 정보는 존재하지 않는 것과 같다.

---

## 3.2 Architectural Constraints (제약 시스템)

AI의 행동을 제한하는 규칙이다.

예

```
AI는 특정 디렉토리만 수정 가능
AI는 production secret 접근 금지
AI는 database schema 직접 수정 금지
```

구현 방법

* Permission system
* Tool allow-list
* Sandbox
* 정책 기반 제어

---

## 3.3 Verification / Evaluation

AI가 만든 결과를 **자동 검증하는 시스템**

예

* Unit Test
* Integration Test
* Lint
* Static analysis
* CI pipeline

흐름 예시

```
AI 코드 생성
→ 테스트 실행
→ 실패 시 수정 요청
```

---

## 3.4 Feedback Loop

AI가 스스로 문제를 수정하도록 하는 반복 구조이다.

예시 workflow

```
1. AI가 코드 생성
2. 테스트 실행
3. 오류 발생
4. 오류 로그 전달
5. AI가 수정
6. 다시 테스트
```

이를 **Self-healing agent loop**라고도 한다.

---

# 4. AI Engineering 계층 구조

현재 AI 개발에서 흔히 이야기되는 계층은 다음과 같다.

```
Prompt Engineering
        ↓
Context Engineering
        ↓
Harness Engineering
```

설명

| 단계                  | 역할                       |
|---------------------|--------------------------|
| Prompt Engineering  | 모델에게 지시 작성               |
| Context Engineering | 모델에게 제공할 데이터 관리          |
| Harness Engineering | AI가 실제 시스템에서 작동하도록 환경 설계 |

즉 **Harness Engineering이 가장 상위 개념**이다.

---

# 5. AI Harness의 주요 구성 요소

실제 시스템에서 Harness는 다음 컴포넌트들로 구성된다.

## Agent Runtime

* agent loop
* task planner
* retry policy

## Tool System

AI가 사용할 수 있는 도구

* API
* CLI
* Database
* Browser
* Code execution

## Memory System

* Vector database
* Task memory
* Long-term memory

## Guardrails

* 정책 기반 제어
* 접근 권한
* 행동 제한

## Evaluation

* Benchmark
* 자동 테스트
* 품질 평가

## Observability

* logs
* trace
* token usage
* 비용 분석

## Entropy Management / Garbage Collection

AI 에이전트가 장기간 운영되면 **엔트로피(무질서도)**가 축적되어 시스템 성능이 저하된다. 이를 방지하기 위한 관리 체계이다.

### 코드베이스 엔트로피 (AI Slope)

AI 에이전트가 대규모로 코드를 생성하면 **패턴 드리프트**와 **기술 부채**가 빠르게 축적된다. 동일한 문제를 서로 다른 방식으로 해결하거나, 프로젝트 컨벤션에서 점진적으로 벗어나는 현상이 발생한다.

대응 전략:

* 백그라운드 에이전트의 주기적 코드 스캔 — 패턴 일관성 검증 및 리팩토링 PR 자동 생성
* 아키텍처 테스트(Konsist 등)를 통한 드리프트 조기 감지
* 스타일/컨벤션 규칙의 정량적 측정 및 추적

### 컨텍스트/메모리 엔트로피

에이전트의 메모리 시스템에 **stale(오래된)하거나 무관한 정보**가 누적되면 추론 비용이 증가하고 오류가 전파된다.

대응 전략:

* **Memory Score** 기반 pruning — `recency × frequency × importance` 점수로 메모리 우선순위 결정
* **Active Forgetting** — 일정 기준 이하의 메모리를 자동 정리하여 컨텍스트 품질 유지
* 주기적 메모리 리뷰 — 중복, 모순, 만료된 정보 제거

### Self-degradation 방지

GC 없이 시간이 지나면 에이전트 성능이 점진적으로 하락하는 **자기 퇴화(Self-degradation)** 현상이 발생한다. 코드베이스 엔트로피와 컨텍스트 엔트로피가 복합적으로 작용하여, 에이전트가 잘못된 패턴을 학습하고 오래된 정보에 기반한 판단을 내리게 된다.

대응 전략:

* 정기적 엔트로피 측정 지표 수립 (패턴 일관성 점수, 메모리 신선도 등)
* 임계값 초과 시 자동 GC 트리거
* 에이전트 성능 메트릭 모니터링 — 작업 성공률, 수정 반복 횟수 추적

---

# 6. Harness Engineering의 핵심 가치

AI 시스템의 성능은 **모델 자체보다 Harness에 더 크게 의존하는 경우가 많다.**

```
Model Quality < Harness Quality
```

같은 모델을 사용해도

```
좋은 Harness → 안정적인 AI 제품
나쁜 Harness → 데모 수준 시스템
```

이 차이가 발생한다.

Harness는 AI를

* 실험적인 기술 → **운영 가능한 시스템**

으로 바꾸는 핵심 요소이다.

---

# 7. Harness Engineering이 적용되는 대표 시스템

## AI Coding Agent

* Claude Code
* Cursor Agent
* Devin

## Agent Framework

* LangChain
* AutoGen
* CrewAI

## Agent Orchestration

* Tool routing
* Agent workflow
* Multi-agent system

---

# 8. 핵심 요약

Harness Engineering은

> **AI 모델을 실제 제품 환경에서 안정적으로 작동하도록 만드는 제어 시스템을 설계하는 엔지니어링 분야**

핵심 구성 요소

1. Context Engineering
2. 행동 제약(Constraints)
3. 결과 검증(Verification)
4. 자동 수정 루프(Feedback loop)
5. 엔트로피 관리/가비지 컬렉션(Entropy Management / GC)

개발 패러다임 변화

```
과거: 사람이 코드 작성
현재: AI가 코드 작성
미래: 인간은 AI가 일할 환경을 설계
```

이 **환경 설계가 Harness Engineering**이다.

---

# References

* https://openai.com/index/harness-engineering
* https://www.nxcode.io/resources/news/harness-engineering-complete-guide-ai-agent-codex-2026
* https://www.salesforce.com/agentforce/ai-agents/agent-harness/
* https://authenti.ca/news/harness-engineering
* https://news.hada.io/topic?id=26874
* https://martinfowler.com/articles/exploring-gen-ai/harness-engineering.html
