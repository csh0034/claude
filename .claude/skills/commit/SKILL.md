---
name: commit
description: 커밋 요청시 Angular Commit Convention 기반으로 staged 변경사항을 분석하고 실행한다
---

다음 절차에 따라 Angular Commit Convention 기반으로 커밋을 작성하라.

## 절차

1. `git diff --staged` 로 스테이징된 변경사항을 확인한다. 스테이징된 파일이 없으면 `git status` 로 변경사항을 확인하고 관련 파일을 스테이징한다.
2. 변경사항을 분석하여 아래 규칙에 따라 커밋 메세지를 작성한다.
3. 커밋을 실행한다. 커밋 메시지 마지막에 반드시 다음을 추가한다:

```
Co-Authored-By: Claude <noreply@anthropic.com>
```

## Angular Commit Convention

```
<type>(<scope>): <subject>

<body>
```

### type (필수)

| type | 설명 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `perf` | 성능 개선 |
| `test` | 테스트 추가/수정 |
| `docs` | 문서/주석 변경 |
| `style` | 포맷팅, 세미콜론 등 (로직 변경 없음) |
| `chore` | 빌드, 의존성, 설정 변경 |
| `ci` | CI/CD 설정 변경 |
| `revert` | 이전 커밋 되돌리기 |

### scope (선택)

변경된 영역. 패키지명 또는 기능명을 소문자로.
예: `member`, `order`, `auth`, `infra`, `build`

### subject (필수)

- 반드시 한글로 작성
- 명령형으로 시작
- 마침표 없음
- 50자 이내

### body (선택)

- 반드시 한글로 작성
- 무엇을, 왜 변경했는지 설명
- subject와 빈 줄로 구분
- 72자 줄바꿈
- 반드시 dash(`-`)로 시작하는 목록 형식으로 작성

## 예시

```
feat(member): 이메일 중복 검사 추가

- 중복 이메일로 회원가입 시도 시 BusinessException을 던지도록 변경.
- MemberRepository.existsByEmail을 통해 도메인 레이어에서 검증.
```

```
refactor(infra): 도메인-JPA 매핑 로직을 MemberMapper로 분리
```

```
test(architecture): common 레이어 의존성 규칙 테스트 추가
```
