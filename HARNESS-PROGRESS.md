# 하네스 진행 상황

1. Context Engineering

- CLAUDE.md or AGNETS.md 는 목차형식으로 작성
- 클린아키텍처 + onion 아키텍처 기반 문서 rule
- test code 작성 rule
- api 컨벤션 rule
- entity 설계 rule
- 대용량, 동시성, 성능 관점 rule ✅

2. 행동 제약(Constraints)

- 아키텍처 강제: konsist 를 통한 아키텍처 테스트 코드 수행 (archunit 과 유사한 kotlin lib)
  - common 레이어 @Service/@Repository/@Controller 사용 금지 검증 ✅
  - Domain Event가 Spring ApplicationEvent 미상속 검증 ✅
  - Query DTO가 application 패키지 위치 검증 ✅
- kotlin linter: ktlint 적용
- kotlin coverege: kover 적용 (jetbrains 에서 만든 jacoco 와 유사한 kotlin lib)
- kotlin 정적분석: detekt 미적용 (kotlin 2.3 아직 ga 미지원)

3. 결과 검증(Verification)

- 코드 변경이후 2번사항 수행후 report 형식으로 작성
- 멀티 퍼소나 리뷰 진행, agent 병렬 실행 (보안, 성능, 유지보수, 아키텍처)
- `/validate` 검증 스킬 추가 ✅

4. 자동 수정 루프(Feedback loop)

- PostToolUse 자동 테스트 훅: .kt 파일 Edit/Write 후 자동 테스트 실행 ✅
- 워크플로우 규칙: 기능 완료 후 check 필수, 멀티 스텝 중간 커밋 규칙 ✅
- 최대 시도 횟수, 개발자 필수 개입 기준 정의 필요

5. 엔트로피 관리(가비지 컬렉션)

- 메모리 GC 규칙: Memory Score 기반 pruning, 90일 미접근 검토 ✅
- `/entropy-scan` 엔트로피 스캔 스킬: 네이밍 드리프트, 중복 로직, 문서-코드 정합성 검증 ✅

## 출처

- https://openai.com/index/harness-engineering/
- https://martinfowler.com/articles/exploring-gen-ai/harness-engineering.html
- https://www.nxcode.io/resources/news/harness-engineering-complete-guide-ai-agent-codex-2026
