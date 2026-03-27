# AI 하네스 엔지니어링: 엔트로피 관리와 가비지 컬렉션 실전 가이드

## 1. 하네스 엔지니어링이란?

하네스(Harness)는 말에게 채우는 굴레에서 유래한 비유다. AI 에이전트라는 강력하지만 방향을 모르는 말에게 제약·피드백 루프·문서·린터·관찰 도구 등의 "굴레"를 씌워, 일관되고 유지보수 가능한 소프트웨어를 생산하도록 만드는 체계를 설계하는 분야다.

이 용어는 2026년 2월 HashiCorp 공동창업자 Mitchell Hashimoto가 블로그에서 처음 사용했고, 직후 OpenAI가 Codex 팀의 5개월 실험 보고서("Harness engineering: leveraging Codex in an agent-first world")를 발표하면서 업계 전반에 퍼졌다. Thoughtworks의 Birgitta Böckeler가 Martin Fowler 사이트에서 이를 분석하며 세 가지 기둥(Context Engineering, Architectural Constraints, Garbage Collection)으로 정리했다.

핵심 통찰은 이렇다: **모델 자체보다 모델을 둘러싼 시스템이 더 중요하다.** LangChain은 모델을 바꾸지 않고 하네스만 변경해서 Terminal Bench 2.0에서 52.8%→66.5%로 점프, Top 30에서 Top 5로 올라갔다.

---

## 2. 엔트로피 문제: 왜 AI 생성 코드는 부패하는가

### 2.1 엔트로피의 정의

AI 에이전트가 생성한 코드베이스는 시간이 지남에 따라 고유한 방식으로 "부패"한다. 이를 **엔트로피(entropy)** 또는 **AI 슬롭(AI slop)** 이라 부른다. 구체적 증상은 다음과 같다:

- **문서 드리프트**: 문서가 실제 코드와 불일치
- **네이밍 컨벤션 분기**: 에이전트가 기존의 서로 다른 패턴을 무분별하게 복제
- **데드 코드 누적**: 더 이상 사용되지 않는 코드가 계속 남음
- **패턴 복제**: 에이전트는 패턴 복제기(pattern replicator)이므로, 좋은 패턴뿐 아니라 나쁜 패턴도 그대로 복제
- **아키텍처 드리프트**: 레이어 간 의존성 규칙을 위반하는 코드가 생성

### 2.2 OpenAI의 실제 경험

OpenAI Codex 팀이 100만 줄 코드를 에이전트만으로 생성하면서 이 문제를 직접 체험했다. 초기에는 매주 금요일(주 20% 시간)을 "AI 슬롭 정리"에 할애했지만, 이 방식은 확장 불가능(doesn't scale)하다는 결론에 이르렀다.

### 2.3 엔트로피 발생 메커니즘 (정보이론적 관점)

Oleg Komissarov의 분석에 따르면, LLM은 "올바른 답"을 선택하는 것이 아니라 "컨텍스트가 주어졌을 때 가장 확률이 높은 답"을 선택한다. 컨텍스트가 넓고 모호할수록 유효한 출력 공간(확률 분포)이 넓어지고, 이것이 곧 높은 엔트로피다. 좋은 에이전트 설계란 이 확률 공간을 좁고, 명시적이고, 의도적으로 만드는 것이다.

---

## 3. 가비지 컬렉션: 엔트로피에 대한 체계적 대응

### 3.1 개념

하네스 엔지니어링에서의 "가비지 컬렉션"은 프로그래밍 언어의 메모리 GC와 유사한 비유다. 에이전트가 생성한 코드베이스에서 주기적으로 불일치, 규칙 위반, 기술 부채를 찾아내고 자동 수정하는 프로세스를 뜻한다.

OpenAI 원문에서 이를 다음과 같이 설명한다: "기술 부채는 고금리 대출과 같다. 쌓아두고 한꺼번에 고통스럽게 처리하는 것보다, 작은 단위로 지속적으로 갚아나가는 것이 거의 항상 낫다."

### 3.2 OpenAI의 가비지 컬렉션 시스템 (실 사례)

OpenAI Codex 팀이 구축한 시스템의 작동 방식:

**① Golden Principles 인코딩**
- "골든 원칙"을 레포지토리에 직접 코드화
- 예시: 공유 유틸리티 패키지를 hand-rolled helper보다 선호, 데이터를 YOLO 방식으로 탐색하지 않고 반드시 경계에서 검증하거나 타입드 SDK를 사용

**② 백그라운드 Codex 태스크**
- 정기 주기로 백그라운드 Codex 태스크가 코드를 스캔
- 골든 원칙으로부터의 이탈을 탐지
- 품질 등급을 업데이트
- 타겟 리팩토링 PR을 자동 오픈

**③ 빠른 리뷰 & 오토머지**
- 대부분의 정리 PR은 1분 이내 리뷰 후 자동 머지
- 기술 부채를 매일 소액씩 갚아나가는 방식

**④ Doc-gardening 에이전트**
- 전용 린터와 CI 잡이 지식 베이스가 최신인지, 교차 링크가 올바른지, 구조가 맞는지 검증
- "doc-gardening" 에이전트가 실제 코드 동작을 반영하지 않는 오래된/폐기된 문서를 스캔하고 수정 PR을 오픈

**추가**
- codex automation, claude code cli loop 기능

---

## 4. 하네스로 엔트로피를 강제 관리하는 방안

### 4.1 아키텍처 제약의 기계적 강제

단순히 "좋은 코드를 작성하라"고 지시하는 것이 아니라, 좋은 코드가 어떤 것인지를 기계적으로 강제한다.

**레이어드 아키텍처 강제**
- 각 레이어는 왼쪽(하위)의 레이어만 import할 수 있음
- 이것은 제안이 아니라 구조적 테스트(structural test)와 CI 검증으로 강제

**커스텀 린터 + 교정 안내**
- 에이전트가 아키텍처 경계를 위반하면, 에러 메시지 자체에 해당 경계가 무엇인지, 왜 존재하는지, 어떻게 수정해야 하는지를 포함
- 에러 메시지가 곧 교육의 순간(teaching moment)이 됨

**Taste Invariants**
- 구조화된 로깅, 네이밍 규칙, 파일 크기 제한 등 "취향 불변식"까지 강제

### 4.2 주기적 클린업 에이전트 구축

NxCode의 가이드에서 정리한 클린업 에이전트 유형:

| 에이전트 유형 | 역할 |
|---|---|
| Documentation consistency agent | 문서가 현재 코드와 일치하는지 검증 |
| Constraint violation scanner | 이전 검사를 우회한 규칙 위반 코드를 탐지 |
| Pattern enforcement agent | 확립된 패턴으로부터의 이탈을 식별하고 수정 |
| Dependency auditor | 순환 의존성이나 불필요한 의존성을 추적·해결 |

### 4.3 Pre-commit Hook & CI 파이프라인

하네스의 가장 기초적이면서도 강력한 구성 요소:

```
[Pre-commit Hook]
  ↓ 코드 포맷팅 검사
  ↓ 타입 체크
  ↓ 커스텀 린터 규칙 검증
  ↓ 아키텍처 의존성 방향 검증
  ↓ 네이밍 컨벤션 검사

[CI Pipeline]
  ↓ 구조적 테스트 (ArchUnit 등)
  ↓ 문서-코드 일치 검증
  ↓ 품질 등급 산출
  ↓ 위반 시 자동 리팩토링 PR 생성
```

### 4.4 Entropy Governance 90일 로드맵 (Epsilla 제안)

**Day 1-30: 탐지 가능성 확보**
- 에이전트 행동 로깅 체계 구축
- 코드 품질 기준선(baseline) 설정

**Day 31-60: 제약 확립**
- 아키텍처 규칙을 CI에 통합
- 커스텀 린터 배포
- AGENTS.md 100줄 이하로 유지, 상세 문서는 링크

**Day 61-90: 엔트로피 거버넌스 구현**
- 자동화된 "가비지 컬렉션" 태스크 도입: 패턴 드리프트를 주기적으로 스캔하고 교정
- 도메인/시스템 레이어별 기술 부채 추적 대시보드 구축
- 코드 리뷰에서 가장 빈번한 인간 피드백을 자동화된 규칙으로 코드화

핵심 원칙: **자율성을 부여하기 전에 탐지 가능성을 확보하고, 처리량을 최적화하기 전에 제약을 확립한다.**

### 4.5 Claude Code / Codex 실전 템플릿 (muraco.ai)

**Machine-checkable DoD (Definition of Done)**
- 에이전트가 "완료"를 선언하기 전에 통과해야 하는 자동 검증 기준을 파일로 정의
- 에이전트 자체의 "완료" 판단에 의존하지 않음

**Restartable Progress Log**
- 세션이 끊겨도 다시 이어서 작업할 수 있도록 진행 상태를 파일로 지속
- 대화가 아니라 환경(파일과 히스토리)에 상태를 저장

**Regression Gate**
- 기존 기능이 깨지지 않았음을 확인하는 회귀 테스트를 에이전트 완료 조건에 포함

**Entropy Control (Principle 6)**
- 주기적 리팩토링, 중복 제거, 네이밍 정규화, 규칙 유지보수를 운영에 내장

---

## 5. 컨텍스트 엔지니어링으로 엔트로피 사전 방지

### 5.1 엔트로피를 낮추는 컨텍스트 설계 패턴

Oleg Komissarov의 프레임워크:

**역할과 차원 축소 (Role & Dimension Narrowing)**
- 모델이 누구인지 정확히 정의: "배포 리스크 평가 에이전트", "요구사항 추출 에이전트" 등
- 역할을 좁히면 유효한 연속(valid continuations)이 줄어들어 엔트로피가 감소

**도메인 경계 선언 (Domain Boundaries)**
- 모델이 추론할 수 있는 것과 없는 것을 명시
- 드리프트를 유발하는 인접 도메인을 배제
- 허용되는 추상화 수준을 지정

**지식 기반 고정 (Knowledge Grounding)**
- 내부 문서, 표준, 레퍼런스 아키텍처, 런북, 과거 의사결정 기록을 첨부
- 누락 정보를 줄이고 추론을 좁히되, 합성을 완전히 제거하지는 않음

**도구 활용 (Tools)**
- 검색으로 최신 데이터 제공
- 계산기로 결정론적 연산
- 린터·스키마 체크로 검증
- 배포 상태·메트릭으로 환경 상태 전달

### 5.2 레포지토리 자체를 컨텍스트로 설계

OpenAI 팀의 핵심 교훈: 에이전트가 인-컨텍스트에서 접근할 수 없는 지식은 에이전트에게 존재하지 않는 것과 같다.

- Google Docs, 채팅 스레드, 사람의 머릿속에 있는 지식은 시스템에 접근 불가
- 실행 계획을 버전 관리되는 레포지토리 아티팩트로 취급
- 기능 추적에 Markdown보다 JSON을 사용 → 에이전트가 구조화된 데이터를 부적절하게 편집하거나 덮어쓸 가능성이 줄어듦
- AGENTS.md는 100줄 이하로, 개별 문서(맵, 실행 계획, 설계 명세)로 링크

---

## 6. 머지 철학의 변화: 수정이 싸면 기다리지 않는다

에이전트가 빠르게 코드를 생산하면, 전통적인 머지 게이트가 병목이 된다. OpenAI의 접근:

- 최소한의 차단(blocking) 머지 게이트와 짧은 수명의 PR
- 테스트 플레이크는 후속 실행으로 처리, 차단하지 않음
- **"수정 비용이 싸고 대기 비용이 비싸면, 처리량을 최적화하고 문제가 드러나면 고쳐라"**
- 품질 검증 지점이 "머지 전 게이트"에서 "지속적 관찰과 피드백"으로 이동

---

## 7. 관찰 도구(Observability)를 하네스에 포함

에이전트가 자기가 생산한 코드를 스스로 디버그하고 검증하려면:

- LogQL을 통한 로그 접근
- PromQL을 통한 메트릭 접근
- DOM 스냅샷·스크린샷 캡처를 통한 UI 검증
- Chrome DevTools Protocol을 에이전트 런타임에 연결

OpenAI 팀은 Victoria Logs / Metrics / Traces 스택을 사용해 이를 구현했다.

---

## 8. 실무 체크리스트: 오늘부터 시작하기

**즉시 적용 가능 (Low-effort, High-impact):**
- [ ] AGENTS.md 작성 (100줄 이내, 테이블오브콘텐츠 역할)
- [ ] Pre-commit hook에 포매팅·타입 체크 추가
- [ ] 아키텍처 의존성 방향 규칙 1개를 CI에 추가

**단기 (1-2주):**
- [ ] 커스텀 린터 1개 작성 (에러 메시지에 교정 안내 포함)
- [ ] 문서-코드 일치 검증 스크립트 작성
- [ ] 에이전트 작업 완료 기준(DoD)을 machine-checkable 파일로 정의

**중기 (1-2개월):**
- [ ] 백그라운드 클린업 에이전트 구축 (주기적 스캔 → 자동 PR)
- [ ] 품질 등급 시스템 도입
- [ ] 기술 부채 대시보드 구축
- [ ] 코드 리뷰 반복 피드백의 자동 규칙화

**장기:**
- [ ] 구조적 테스트 프레임워크 (ArchUnit 등) 도입
- [ ] 관찰 도구를 에이전트 런타임에 연결
- [ ] 멀티 에이전트 하네스 설계

---

## 9. 출처

- OpenAI, "Harness engineering: leveraging Codex in an agent-first world" (2026.02)
- Birgitta Böckeler, "Harness Engineering" on martinfowler.com
- Mitchell Hashimoto, AI adoption stages blog post (2026.02)
- Oleg Komissarov, "Agentic Foundation: Managing Entropy in AI Systems" (Medium, 2026.01)
- NxCode, "Harness Engineering: The Complete Guide" (2026.03)
- Epsilla Blog, "Harness Engineering: Why the Focus is Shifting" (2026.03)
- Octopus Deploy, "Harness Engineering - The Power of AI, Guided By Human Intelligence"
- muraco.ai, "Harness Engineering 101: Make Claude Code / Codex Consistently Deliver"
- codenote.net, "Harness Engineering — The New Discipline Powering Software Development"
- HumanLayer, "Skill Issue: Harness Engineering for Coding Agents"
