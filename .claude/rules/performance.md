---
paths:
  - src/main/kotlin/**/*
---

# 대용량/동시성/성능 규칙

## 대용량 데이터 처리

- IMPORTANT: 전체 조회(`findAll()`) 금지. 반드시 페이징(`Pageable`) 또는 커서 기반 조회를 사용한다
- IMPORTANT: 대량 INSERT/UPDATE 시 `saveAll()` 대신 JDBC batch insert 또는 `@Modifying` 벌크 쿼리를 사용한다
- 컬렉션을 메모리에 전부 로드하지 않는다. Stream 또는 chunked 처리를 사용한다
- API 응답에 무제한 리스트를 반환하지 않는다. 항상 페이징 또는 `limit`을 적용한다

## 동시성 제어

- IMPORTANT: 동시 수정 가능한 엔티티에는 `@Version`(낙관적 락) 또는 비관적 락(`@Lock`)을 적용한다
- 분산 환경에서의 동시성 제어가 필요한 경우 Redis 분산 락 또는 DB advisory lock을 검토한다
- `@Async` 사용 시 반드시 커스텀 `TaskExecutor`를 설정한다. 기본 `SimpleAsyncTaskExecutor`(스레드 무한 생성) 사용 금지
- 코루틴 사용 시 `Dispatchers.IO`로 블로킹 I/O를 감싼다

## 성능 관점

- IMPORTANT: N+1 문제를 방지한다. `@EntityGraph` 또는 `fetch join`을 사용한다
- IMPORTANT: 인덱스가 필요한 조회 조건에는 `@Index`를 명시한다
- 읽기 전용 조회에는 `@Transactional(readOnly = true)`를 사용한다
- 불필요한 영속성 컨텍스트 로딩을 피한다. 조회 전용 DTO 프로젝션 사용을 고려한다
- 외부 API 호출 시 타임아웃(`connectTimeout`, `readTimeout`)을 반드시 설정한다
- 캐싱이 적합한 데이터(변경 빈도 낮음, 조회 빈도 높음)에는 `@Cacheable` 적용을 고려한다
