---
name: multi-review
description: "여러 전문가 관점에서 코드 리뷰를 진행한다"
---

# 멀티 퍼소나 코드 리뷰

## 리뷰 관점
- **Security**: 보안 취약점 (인젝션, 인증 우회 등)
- **Performance**: 성능 이슈 (N+1, 메모리 누수 등)
- **Maintainability**: 유지보수성 (복잡도, 결합도 등)
- **Testing**: 테스트 커버리지와 품질

## 실행 방법
Task를 사용해서 각 관점별로 병렬 리뷰 수행.
각 Task 결과를 종합하여 우선순위별 리포트 생성.

## 출력 형식
### 🔴 Critical
### 🟠 Major
### 🟡 Minor
### 💡 Suggestions
