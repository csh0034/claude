---
paths:
  - src/main/kotlin/**/*
  - src/test/kotlin/**/*
---

# 코드 탐색 규칙 (LSP 우선)

코드 탐색 시 Grep/Glob보다 LSP를 먼저 사용한다. LSP는 코드의 의미적 구조를 이해하므로 텍스트 검색보다 정확하다.

## LSP 오퍼레이션 우선 사용

| 목적 | LSP (우선) | Grep/Glob (대체) |
|------|-----------|-----------------|
| 정의 위치 찾기 | `goToDefinition` | 사용하지 않음 |
| 인터페이스 구현체 찾기 | `goToImplementation` | 사용하지 않음 |
| 모든 참조/호출 지점 찾기 | `findReferences` | 사용하지 않음 |
| 타입/문서 정보 확인 | `hover` | 파일을 직접 Read |
| 파일 내 심볼 목록 | `documentSymbol` | 사용하지 않음 |
| 프로젝트 전체 심볼 검색 | `workspaceSymbol` | Glob으로 파일명 검색 |
| 호출 계층 추적 | `incomingCalls` / `outgoingCalls` | 사용하지 않음 |
| 텍스트/패턴 검색 (주석, 설정값, 문자열) | 해당 없음 | Grep 사용 |
| 파일 경로/이름 검색 | 해당 없음 | Glob 사용 |

## 필수 워크플로우

- **수정 전**: `goToDefinition` → `findReferences`로 영향 범위 파악 → 수정
- **리팩토링 전**: `findReferences` + `goToImplementation` + `incomingCalls`/`outgoingCalls`로 전체 영향 파악
- **수정 후**: LSP diagnostics 확인하여 에러 없는 상태에서만 다음 작업 진행

## Grep/Glob은 보조 수단

- Grep: 주석, 문자열 리터럴, 설정 파일 등 **코드가 아닌 텍스트** 검색에만 사용
- Glob: 파일 경로/이름 패턴으로 파일을 찾을 때만 사용
- LSP 사용 불가 시에만 Grep을 코드 탐색 대체로 사용
