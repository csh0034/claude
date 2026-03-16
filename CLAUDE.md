# CLAUDE.md

## 프로젝트 개요

이 프로젝트는 Gradle을 빌드 시스템으로 사용하는 Kotlin Spring Boot 웹 애플리케이션입니다.

- **Kotlin**: 2.3.10
- **Spring Boot**: 4.0.3
- **Java**: 25
- **Gradle**: 9.3.1 (wrapper 통해 사용)

## 빌드 및 검증 명령어

- `./gradlew build` — 전체 빌드 (컴파일 + 테스트 + ktlint + koverVerify 포함)
- `./gradlew check` — 테스트 + 정적분석 + 커버리지 검증 (ktlintCheck, koverVerify 자동 포함)
- `./gradlew test` — 테스트만 실행
- `./gradlew ktlintFormat` — 코드 포맷 자동 수정
- `./gradlew koverHtmlReport` — 커버리지 HTML 리포트 생성 (`build/reports/kover/html/`)
- `./gradlew koverVerify` — 커버리지 임계값 검증 (전체 70%)
