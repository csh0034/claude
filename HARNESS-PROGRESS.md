# 하네스 진행 상황

1. Context Engineering

- CLAUDE.md or AGNETS.md 는 목차형식으로 작성
- 클린아키텍처 + onion 아키텍처 기반 문서 rule
- test code 작성 rule
- api 컨벤션 rule
- entity 설계 rule
- 대용량, 동시성, 성능 관점 rule (추가 예정)

2. 행동 제약(Constraints)

- 아키텍처 강제: konsist 를 통한 아키텍처 테스트 코드 수행 (archunit 과 유사한 kotlin lib)
- kotlin linter: ktlint 적용
- kotlin coverege: kover 적용 (jetbrains 에서 만든 jacoco 와 유사한 kotlin lib)
- kotlin 정적분석: detekt 미적용 (kotlin 2.3 아직 ga 미지원)

3. 결과 검증(Verification)

- 코드 변경이후 2번사항 수행후 report 형식으로 작성
- 멀티 퍼소나 리뷰 진행, agent 병렬 실행 (보안, 성능, 유지보수, 아키텍처)

4. 자동 수정 루프(Feedback loop)

- 3번사항 결과 기반 자동 수정 및 선택 필요시 개발자 개입
- 최대 시도 횟수, 개발자 필수 개입 기준 정의 필요

5. 엔트로피 관리(가비지 컬렉션)

- 시간이 지나면서 md의 규칙과 실제 코드가 어긋나기 시작하므로, 주기적으로 문서-코드 정합성을 점검하는 에이전트 추가 (검토중)

## 출처

- https://openai.com/index/harness-engineering/
- https://martinfowler.com/articles/exploring-gen-ai/harness-engineering.html
- https://www.nxcode.io/resources/news/harness-engineering-complete-guide-ai-agent-codex-2026
