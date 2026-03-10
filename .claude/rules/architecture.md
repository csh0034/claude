# Kotlin Spring 클린 아키텍처 규칙 (Onion Architecture)

## 의존 방향

`ui → application → domain ← infra`, 모든 레이어 `→ common`, `common`은 어디에도 의존 금지

- IMPORTANT: 의존성은 항상 안쪽(Domain)을 향한다
- IMPORTANT: Domain은 어떤 외부 패키지도 의존하지 않는다 (common의 순수 Kotlin 요소 제외)
- IMPORTANT: Output Port는 domain에 interface로 정의하고(`MemberPort` 등), infra의 Adapter가 이를 구현(implements)한다(`MemberAdapter` 등) (의존성 역전)
- IMPORTANT: common은 모든 레이어에서 import 가능하지만, common 자체는 어떤 레이어도 import하지 않는다
- Application은 Port 인터페이스만 알고, 구현체(Adapter)는 모른다

IMPORTANT: 단일 모듈이므로 Gradle이 의존 방향을 강제하지 못한다. 반드시 아래 import 규칙을 준수하며, 코드 리뷰 시 역방향 import를 반드시 체크한다.

### 패키지 import 규칙

| 패키지 | import 허용 | import 금지 |
|--------|------------|------------|
| `domain` | `common` (순수 Kotlin 요소만) | `application`, `infra`, `ui`, Spring/JPA 전부 |
| `application` | `domain`, `common` | `infra`, `ui` |
| `infra` | `domain`, `application`, `common` | `ui` |
| `ui` | `application`, `common` | `domain` 직접 참조 최소화, `infra` 금지 |
| `common` | 없음 (다른 레이어 패키지 의존 금지) | `domain`, `application`, `infra`, `ui` |

- IMPORTANT: `domain` 패키지는 `common`의 순수 Kotlin 요소(상수, 기본 예외)만 사용 가능. Spring 설정 클래스 import 금지
- IMPORTANT: `ui`에서 `infra`를 직접 import 금지
- IMPORTANT: `application`에서 `infra`를 직접 import 금지

## 레이어별 구성

- **domain**: Entity(순수 Kotlin 클래스), VO, Output Port(interface — `MemberPort` 등), Domain Event. 프레임워크 어노테이션 절대 금지
- **application**: Input Port(UseCase interface — `CreateMemberUseCase` 등), Service(구현체, `@Service`/`@Transactional` 허용), Command/Query/Result DTO
- **infra**: JpaEntity(`@Entity`), JpaRepository, Adapter(domain Output Port의 구현체 — `MemberAdapter` 등, `@Repository`), Mapper(Domain↔JPA 변환)
- **ui**: Controller(`@RestController`), Request/Response DTO
- **common**: 횡단 관심사만 — constant/, exception/, config/(`@Configuration`), extension/

`Application.kt`(`@SpringBootApplication`)는 프로젝트 루트 패키지에 위치.

## 핵심 규칙

- IMPORTANT: Domain Entity ≠ JPA Entity. JPA Entity는 infra에만 존재
- IMPORTANT: Domain Event는 순수 data class. Spring ApplicationEvent를 상속하지 않는다
- IMPORTANT: UseCase는 Port 인터페이스만 의존, 구현체(Adapter) 직접 참조 금지
- IMPORTANT: UseCase 하나는 하나의 기능만 수행 (단일 책임)
- IMPORTANT: Application DTO(Command/Result)와 UI DTO(Request/Response)는 별개 클래스
- Controller는 UseCase(Input Port)만 의존. Service 구현체 직접 참조 금지
- Request→Command 변환은 Controller 또는 Request DTO에서, Result→Response 변환은 Response DTO의 companion object에서 수행

## 네이밍 컨벤션

| 패키지 | 종류                       | 접미사                                                              |
|--------|--------------------------|------------------------------------------------------------------|
| domain | Entity / VO              | (없음)                                                             |
| domain | Output Port              | `Port`                                                           |
| domain | Domain Event             | `Event`                                                          |
| domain | Domain Service           | `DomainService`                                                  |
| application | Input Port               | `UseCase`                                                        |
| application | UseCase 구현               | `Service`                                                        |
| application | 입력 DTO                   | `Command` / `Query`                                              |
| application | 출력 DTO                   | `Result`                                                         |
| infra | JPA Entity               | `JpaEntity`                                                      |
| infra | JPA Repository           | `JpaRepository`                                                  |
| infra | Output Port 구현체          | `Adapter`                                                        |
| infra | 매퍼                       | `Mapper`                                                         |
| ui | Controller               | `Controller`                                                     |
| ui | HTTP Request             | `Request`                                                        |
| ui | HTTP Response            | `Response`                                                       |
| common | 상수 / 에러코드 / 예외 / 설정 / 확장 | `Constants` / `ErrorCode` / `Exception` / `Config` / `Extension` |
