# 메모리 GC (Garbage Collection) 규칙

## 목적

`.claude/memory/` 내 메모리 파일의 품질과 관련성을 유지하여 AI 에이전트의 자기 퇴화(Self-degradation)를 방지한다.

## Pruning 기준

### Memory Score

각 메모리의 유지/제거 판단은 다음 세 가지 요소의 가중합으로 결정한다:

```
Memory Score = Recency(0.3) × Frequency(0.3) × Importance(0.4)
```

- **Recency**: 최근 접근/갱신 시점 (90일 이상 미접근 → 0점)
- **Frequency**: 대화에서 참조된 빈도 (높을수록 고점)
- **Importance**: 메모리 타입별 기본 중요도
  - `feedback` → 높음 (사용자 교정 사항)
  - `user` → 중간 (사용자 프로파일)
  - `project` → 낮음 (빠르게 변하는 상태)
  - `reference` → 중간 (외부 시스템 포인터)

### 자동 검토 대상

- IMPORTANT: 90일 이상 미접근 메모리는 검토 대상으로 표시한다
- IMPORTANT: 동일 주제의 중복 메모리가 발견되면 최신 것으로 병합하고 나머지를 제거한다
- `project` 타입 메모리 중 날짜가 명시된 항목은 해당 날짜 경과 후 유효성을 재검토한다

## GC 실행 시점

- `/entropy-scan` 스킬 실행 시 메모리 GC도 함께 수행
- 사용자가 명시적으로 메모리 정리를 요청할 때
- 메모리 파일이 20개를 초과할 때 자동으로 GC 검토 제안

## GC 절차

1. `MEMORY.md` 인덱스와 실제 메모리 파일 목록을 대조하여 불일치 확인
2. 각 메모리 파일의 Memory Score를 산출
3. Score가 임계값(0.3) 미만인 메모리를 사용자에게 제거 후보로 제안
4. 사용자 승인 후 제거 및 `MEMORY.md` 인덱스 갱신
