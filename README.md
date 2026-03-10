# Claude Code 모범 사례 — 먼저 탐색, 계획, 코드 작성

> 출처: https://code.claude.com/docs/ko/best-practices

## 핵심 원칙

Claude Code는 에이전트 코딩 환경으로, 파일을 읽고 명령을 실행하며 자율적으로 문제를 해결한다.
가장 중요한 제약은 **context window**다. 채워질수록 성능이 저하되므로 적극적으로 관리해야 한다.

---

## 1. 작업 검증 방법을 제공하라

가장 영향력 높은 단일 행동. 테스트, 스크린샷, 예상 출력을 포함하면 Claude가 스스로 작업을 검증할 수 있다.

| 전략 | Before | After |
|------|--------|-------|
| 검증 기준 제공 | "이메일 검증 함수 구현" | "validateEmail 작성. user@example.com → true, invalid → false. 구현 후 테스트 실행" |
| UI 시각 검증 | "대시보드를 더 좋게" | "[스크린샷] 이 디자인 구현 → 결과 스크린샷 → 원본 비교 → 차이점 수정" |
| 근본 원인 해결 | "빌드 실패" | "이 오류로 빌드 실패: [오류]. 수정 후 빌드 성공 확인. 근본 원인 해결" |

---

## 2. 먼저 탐색 → 계획 → 코드 작성

연구와 계획을 구현과 분리하여 **잘못된 문제를 해결하는 것**을 방지한다.

### 4단계 워크플로우

1. **탐색** (Plan Mode): 파일을 읽고 변경 없이 코드베이스를 이해
   ```
   /src/auth를 읽고 세션 및 로그인 처리 방식을 이해하십시오.
   ```

2. **계획** (Plan Mode): 상세 구현 계획 작성
   ```
   Google OAuth를 추가하려면 어떤 파일을 변경해야 하나? 세션 흐름은? 계획을 작성하라.
   ```
   - `Ctrl+G`로 계획을 텍스트 편집기에서 직접 편집 가능

3. **구현** (Normal Mode): 계획에 따라 코드 작성 및 검증
   ```
   계획에서 OAuth 흐름을 구현하라. 콜백 핸들러 테스트 작성, 테스트 실행 후 실패 수정.
   ```

4. **커밋**: 설명적 메시지로 커밋 및 PR 생성

### 언제 계획을 건너뛸까?

- diff를 한 문장으로 설명할 수 있으면 계획 불필요
- 오타 수정, 로그 추가, 변수 이름 변경 등 범위가 명확한 작업
- 접근 방식이 불확실하거나, 여러 파일을 수정하거나, 코드에 익숙하지 않을 때 계획이 유용

---

## 3. 구체적인 컨텍스트를 제공하라

| 전략 | Before | After |
|------|--------|-------|
| 작업 범위 지정 | "foo.py 테스트 추가" | "사용자 로그아웃 엣지 케이스 테스트 작성. 모의 객체 피하기" |
| 소스 지적 | "ExecutionFactory API가 왜 이상한가?" | "ExecutionFactory git 히스토리를 보고 API 변천 요약" |
| 기존 패턴 참조 | "캘린더 위젯 추가" | "HotDogWidget.php 패턴을 따라 캘린더 위젯 구현. 기존 라이브러리만 사용" |
| 증상 설명 | "로그인 버그 수정" | "세션 시간 초과 후 로그인 실패. src/auth/ 토큰 새로고침 확인. 실패 테스트 작성 후 수정" |

### 풍부한 콘텐츠 제공 방법

- `@`로 파일 참조
- 이미지 복사/붙여넣기
- URL로 문서/API 참조 제공
- `cat error.log | claude`로 데이터 파이프
- Bash, MCP 도구, 파일 읽기로 Claude가 직접 컨텍스트 수집

---

## 4. 환경 구성

### CLAUDE.md 작성

- `/init`으로 초기 파일 생성 후 점진적 개선
- Bash 명령, 코드 스타일, 워크플로우 규칙 포함
- **간결하게 유지**: "이것을 제거하면 Claude가 실수할까?" — 아니면 삭제
- 팀과 공유하려면 git에 체크인

### 권한 구성

- `/permissions`으로 안전한 명령 허용 목록 추가
- `/sandbox`로 OS 수준 격리

### CLI 도구 활용

- `gh`, `aws`, `gcloud`, `sentry-cli` 등 외부 CLI 도구 활용
- `--help`로 도구를 학습시킬 수도 있음

### MCP 서버, hooks, skills, subagents, plugins

- `claude mcp add`로 외부 도구 연결 (Notion, Figma, DB 등)
- hooks: 예외 없이 매번 발생해야 하는 작업 자동화
- skills: `.claude/skills/`에 도메인 지식 및 재사용 워크플로우 정의
- subagents: `.claude/agents/`에 전문화된 어시스턴트 정의
- plugins: `/plugin`으로 마켓플레이스에서 설치

---

## 5. 세션 관리

### 방향 수정

- `Esc`: 작업 중지 (context 보존)
- `Esc + Esc` 또는 `/rewind`: 이전 상태 복원
- `"Undo that"`: 변경 사항 되돌리기
- `/clear`: 관련 없는 작업 간 context 재설정
- 같은 문제에 2번 이상 수정했으면 `/clear` 후 새 프롬프트로 재시작

### context 관리

- 작업 간 `/clear` 자주 사용
- `/compact <instructions>`로 압축 제어
- CLAUDE.md에 압축 시 보존할 내용 지시 가능

### subagents로 조사

- 별도 context window에서 탐색 → 주요 대화를 깨끗하게 유지
  ```
  subagents를 사용하여 인증 시스템의 토큰 새로고침 방식을 조사하라.
  ```

### 대화 재개

- `claude --continue`: 최근 대화 재개
- `claude --resume`: 최근 대화 목록에서 선택
- `/rename`으로 세션에 설명적 이름 지정

---

## 6. 자동화 및 확장

### 비대화형 모드

```bash
claude -p "프롬프트"                          # 일회성
claude -p "프롬프트" --output-format json      # 구조화된 출력
claude -p "프롬프트" --output-format stream-json  # 스트리밍
```

### 병렬 세션

- Claude Code 데스크톱 앱, 웹, Agent teams로 병렬 실행
- Writer/Reviewer 패턴: 한 세션은 구현, 다른 세션은 리뷰

### Fan-out 패턴

```bash
for file in $(cat files.txt); do
  claude -p "React에서 Vue로 $file 마이그레이션" \
    --allowedTools "Edit,Bash(git commit *)"
done
```

---

## 7. 일반적인 실패 패턴

| 패턴 | 문제 | 해결 |
|------|------|------|
| 주방 싱크 세션 | 관련 없는 작업이 context를 오염 | 작업 간 `/clear` |
| 반복 수정 | 실패한 접근 방식이 context를 오염 | 2번 실패 후 `/clear` + 더 나은 프롬프트 |
| 과도한 CLAUDE.md | 중요한 규칙이 노이즈에 묻힘 | 무자비하게 정리 |
| 신뢰-검증 간격 | 그럴듯하지만 실제로 작동 안 함 | 항상 검증 제공 |
| 무한 탐색 | 범위 없는 조사로 context 소진 | 범위 좁히기 또는 subagents 사용 |
