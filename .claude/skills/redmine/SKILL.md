---
name: redmine
description: Redmine 이슈를 조회하고 구현한 뒤 결과를 댓글로 등록한다
---

# Redmine 이슈 기반 개발 워크플로우

인자로 받은 Redmine 이슈 번호를 기반으로 조회 → 구현 → 리뷰 → 댓글 등록까지 자동화한다.

## 워크플로우

### 1단계: 환경 변수 검증

`$REDMINE_URL`과 `$REDMINE_API_KEY` 환경 변수를 확인한다.
둘 중 하나라도 없으면 아래 안내를 출력하고 중단한다:

```
Redmine 연동에 필요한 환경 변수가 설정되지 않았습니다.

export REDMINE_URL="https://your-redmine.example.com"
export REDMINE_API_KEY="your-api-key"
```

### 2단계: 이슈 조회

```bash
curl -s -H "X-Redmine-API-Key: $REDMINE_API_KEY" "$REDMINE_URL/issues/{번호}.json"
```

응답에서 다음 필드를 추출하여 표로 표시한다:
- subject (제목)
- description (설명)
- tracker (유형)
- priority (우선순위)
- status (상태)

이슈를 찾을 수 없으면 에러 메시지를 출력하고 중단한다.

### 3단계: 구현 계획 수립

이슈 내용을 분석하여 구현 계획을 수립한다:
- Onion Architecture 규칙(`.claude/rules/architecture.md`)을 준수한다
- 영향받는 레이어(domain, application, infra, ui)를 식별한다
- 변경/생성할 파일 목록과 각 파일의 역할을 정리한다

계획을 사용자에게 제시하고 확인을 받은 뒤 다음 단계로 진행한다.

### 4단계: 코드 구현

TDD(Red → Green → Refactor) 방식으로 구현한다:
- domain → application → infra → ui 순서로 진행한다
- 각 레이어 구현 후 `./gradlew test`를 실행하여 테스트를 검증한다
- 테스트 실패 시 수정 후 재실행한다

### 5단계: 멀티 리뷰

`/multi-review` skill을 호출하여 코드 리뷰를 수행한다.
Critical 또는 Major 이슈가 발견되면 수정 후 재리뷰한다.

### 6단계: 사용자 확인

다음 내용을 요약하여 사용자에게 제시한다:
- 변경된 파일 목록
- 테스트 실행 결과
- 리뷰 결과 요약

사용자에게 Redmine 댓글 등록 여부를 확인한다.

### 7단계: Redmine 댓글 등록

사용자가 승인하면 구현 결과를 Redmine 이슈에 댓글로 등록한다:

```bash
curl -s -X PUT \
  -H "X-Redmine-API-Key: $REDMINE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"issue": {"notes": "댓글 내용"}}' \
  "$REDMINE_URL/issues/{번호}.json"
```

댓글에는 다음 내용을 포함한다:
- 구현 요약
