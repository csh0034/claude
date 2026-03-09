---
paths:
  - src/main/kotlin/**/ui/**/*
---

# REST API 컨벤션 (ui 레이어)

## URL 설계

- 복수형 명사 사용: `/members`
- 케밥 케이스: `/order-items`
- 동사 사용 금지: `POST /members` (create 붙이지 않음)

## HTTP 메서드 매핑

| 동작 | Method | URL | 상태코드 |
|------|--------|-----|----------|
| 목록 조회 | GET | `/members` | 200 |
| 단건 조회 | GET | `/members/{id}` | 200 |
| 생성 | POST | `/members` | 201 |
| 전체 수정 | PUT | `/members/{id}` | 200 |
| 부분 수정 | PATCH | `/members/{id}` | 200 |
| 삭제 | DELETE | `/members/{id}` | 204 |

## Controller 작성 규칙

```kotlin
@RestController
@RequestMapping("/members")
class MemberController(
    private val createMemberUseCase: CreateMemberUseCase,  // Input Port만 의존
    private val getMemberUseCase: GetMemberUseCase,
) {
    @PostMapping
    fun create(@Valid @RequestBody request: MemberCreateRequest): ResponseEntity<ApiResponse<MemberResponse>> {
        val command = request.toCommand()
        val result = createMemberUseCase.execute(command)
        val response = MemberResponse.from(result)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(201, "회원 생성 완료", response))
    }
}
```

- IMPORTANT: Controller는 UseCase(Input Port) 인터페이스만 의존
- IMPORTANT: Controller에 비즈니스 로직 금지, infra 패키지 import 금지
- Request → Command 변환, Result → Response 변환만 수행
- 생성자 주입만 사용 (`@Autowired` 필드 주입 금지)

## DTO 변환 체인

```
MemberCreateRequest (ui)  →  .toCommand()  →  CreateMemberCommand (application)
MemberResult (application)  →  MemberResponse.from(result)  →  MemberResponse (ui)
```

- IMPORTANT: ui DTO와 application DTO는 별개 클래스
- Request DTO에 `@field:` Validation 어노테이션 사용 (Kotlin 특성)

## UI DTO 예시1

```kotlin
data class MemberCreateRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,
    @field:Email(message = "이메일 형식이 아닙니다")
    val email: String,
) {
    fun toCommand() = CreateMemberCommand(name = name, email = email)
}

data class MemberResponse(
    val id: Long,
    val name: String,
    val email: String,
) {
    companion object {
        fun from(result: MemberResult) = MemberResponse(
            id = result.id, name = result.name, email = result.email,
        )
    }
}
```

## 통일된 응답 형식

```kotlin
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,
)
```
