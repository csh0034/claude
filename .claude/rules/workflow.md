# 워크플로우 규칙

## 기능 완료 후 검증

- IMPORTANT: 기능 구현 또는 버그 수정 완료 후 반드시 `./gradlew check`를 실행하여 전체 검증을 통과시킨다
- `check` 태스크는 컴파일, ktlint, 테스트, koverVerify를 모두 포함한다
- 검증 실패 시 커밋하지 않고 실패 원인을 먼저 해결한다

## 멀티 스텝 작업 시 중간 커밋

- IMPORTANT: 여러 레이어에 걸친 기능 구현 시 레이어별로 중간 커밋을 수행한다
- 커밋 순서: domain → application → infra → ui (안쪽 레이어부터)
- 각 중간 커밋 전 `./gradlew test --fail-fast`로 최소 검증을 수행한다
- 최종 커밋 전에는 반드시 `./gradlew check`로 전체 검증을 수행한다

## 커밋 전 체크리스트

1. 코드 변경이 아키텍처 규칙(`.claude/rules/architecture.md`)을 준수하는가
2. 테스트가 추가/수정되었는가 (TDD 사이클 준수)
3. `./gradlew check` 전체 통과 확인
4. 불필요한 import, 미사용 변수 정리 완료
