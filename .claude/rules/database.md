---
paths:
  - src/main/kotlin/**/infra/persistence/**/*
  - src/main/kotlin/**/domain/**/*Repository*
---

# Database / JPA 규칙 (infra 레이어)

## 핵심 원칙

- IMPORTANT: Domain Entity ≠ JPA Entity. JPA Entity는 infra 패키지에만 존재
- IMPORTANT: `data class`로 JPA Entity 만들지 않음 (equals/hashCode 이슈)
- IMPORTANT: JPA Repository는 infra 내부에서만 사용. Domain의 Output Port와 별개 클래스
- IMPORTANT: `FetchType.LAZY` 기본 사용 (EAGER 금지). N+1은 `@EntityGraph` 또는 `fetch join`으로 해결

## JPA Entity 작성 규칙

- `@Enumerated(EnumType.STRING)` 사용 (ORDINAL 금지)
- 연관관계: `@ManyToOne(fetch = LAZY)` 기본. Domain Entity의 연관은 ID 참조 또는 별도 조회

## Adapter / Mapper

- Adapter(`@Repository`)는 domain Output Port를 구현하고, Domain↔JPA 변환을 Mapper에 위임
- Mapper(`@Component`)는 `toDomain`, `toJpaEntity` 양방향 변환 함수 필수
- Enum 변환: `name` ↔ `valueOf`
- `findByIdOrNull()` 사용 (Optional 대신 Kotlin nullable)
