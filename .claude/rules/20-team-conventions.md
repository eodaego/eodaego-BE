# Team Conventions

## Naming

- 변수명 규칙: 축약어 사용 금지, 전체 단어 사용(예: `request`를 `req`로 줄이지 않음). 단, `id`, `url`처럼 업계 표준으로 굳어진 축약어는 예외로 허용.
- 메서드명 규칙: 축약어 금지(위와 동일). 동사로 시작(`findBy...`, `createAccessToken`, `resolveNickname` 등).
- 클래스 / 컴포넌트명 규칙: 역할이 이름에 드러나야 함(`AuthService`, `AuthController`, `AuthControllerDocs`, `JwtProvider` 등). Controller는 반드시 `XxxController`, 그 Swagger 문서 인터페이스는 `XxxControllerDocs`.
- 축약 금지 여부 및 예외: 원칙적으로 금지. `id`, `url`, `dto` 등 이미 업계에서 축약형 자체가 표준인 경우만 예외.

## Code style

- 명시적 타입 사용 규칙: Java 표준을 따름(var 사용은 지역 변수 타입이 명확할 때만, 필드/파라미터/반환 타입은 명시).
- any 사용 여부: 해당 없음(Java는 정적 타입 언어). `Object` 타입 남용 금지.
- deprecated API 사용 기준: 신규 코드에서 사용 금지. 기존 코드에 deprecated API가 남아있다면 손대는 김에 교체(단, 이번 작업 범위를 벗어나는 대규모 교체는 별도 논의).
- 에러 처리 방식: 도메인 예외는 `CustomException(ErrorCode)`로 던지고, `global/exception/GlobalExceptionHandler`(`@RestControllerAdvice(annotations = RestController.class)`)에서 일괄 처리해 `ErrorResponse`로 변환. `annotations = RestController.class` 스코프 덕분에 `@RestController`(JSON API)에서 발생한 예외만 처리하고, `@Controller`(관리자 Thymeleaf MVC)에서 발생한 예외는 자동으로 대상에서 제외된다 — 새 도메인이 추가돼도 별도 설정 없이 자동 적용됨. 새로운 실패 케이스가 생기면 `ErrorCode`에 상수를 추가.
- 주석 작성 기준: 한국어로 작성. WHY(왜 이렇게 했는지)가 비자명한 경우에만 작성하고, 코드가 이미 보여주는 WHAT은 다시 설명하지 않음.

## Entity

- `@Getter @Setter` 사용(Lombok). 별도 도메인 메서드(예: `rotate()`, `updatePassword()`, 정적 팩토리 메서드 `create()`/`register()` 등)는 만들지 않는다 — 생성은 `@Builder`, 변경은 Setter로 통일.
- `@Builder` 패턴 사용. `@AllArgsConstructor` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 함께 사용해 Builder가 동작하도록 한다.
- `@Table`, `@Column`의 `name` 속성은 명시하지 않는다(Hibernate 기본 네이밍 전략에 위임). 단, `uniqueConstraints`처럼 이름 지정이 아닌 제약조건 자체를 위한 설정은 예외적으로 사용 가능.
- 모든 Entity는 `BaseEntity`(`global/entity/BaseEntity.java` — `createdAt`/`updatedAt` Auditing)를 상속한다.
- PK는 전부 `UUID` 타입(`@GeneratedValue(strategy = GenerationType.UUID)`).
- 엔티티 간 연관관계는 `@ManyToOne`, `@OneToOne`, `@OneToMany` 등 JPA 어노테이션으로 표현한다(원시 FK 필드 금지).
- Enum 필드는 `@Enumerated(EnumType.STRING)`으로 저장한다.
- boolean 필드는 `boolean firstLogin`처럼 명시한다 — `Boolean` 래퍼 타입이나 `is` 접두사(`isFirstLogin`)를 필드명에 사용하지 않는다.

## DTO

- `record`로 작성한다(Request/Response 공통).
- Bean Validation(`@NotBlank`, `@NotNull` 등, Spring Validator)을 사용한다.
- `dto` 패키지 내부에 `request`, `response` 하위 패키지를 만들어 요청/응답 클래스를 분리한다.
- REST API(`controller`)에서 사용하는 모든 Request/Response DTO 필드에는 `@Schema(description = ..., example = ...)`를 붙인다(자세한 규칙은 아래 "Swagger 문서화" 참고).

## Repository

- Spring Data JPA를 기본으로 사용한다.
- JPQL 사용은 지양하고, 필요한 경우 네이티브 SQL을 사용한다.
- `getReferenceById`는 지양하고 `findById`를 사용한다(프록시 참조로 인한 예외 상황 방지). 조회 실패 시 적절한 `ErrorCode`(예: `MEMBER_NOT_FOUND`)로 `CustomException`을 던진다.

## Service

- 모든 비즈니스 로직은 Service에 위치한다. Controller는 요청/응답 처리만 담당한다(분기/변환/조건문 없음).
- `@Slf4j`를 사용해 적절히 로깅한다. 로깅 레벨 기준:
  - `INFO`: 주요 비즈니스 이벤트(로그인 성공, 회원가입, 관리자 계정 초기화 등)
  - `WARN`: 예상된 실패(`CustomException`으로 이어지는 검증 실패, 잘못된 토큰 등)
  - `ERROR`: 예상하지 못한 예외(외부 시스템 연동 실패 등 복구 불가능한 상황)
  - `DEBUG`: 상세 파라미터, 내부 흐름 추적용(운영 환경에서는 비활성)
- 생성자 주입 시 가능한 경우 `@RequiredArgsConstructor`를 사용한다(모든 의존성은 `private final`).

## Controller

- `domain.<도메인>.controller`는 REST API(`@RestController`)를 위한 패키지다. Web MVC 페이지를 반환하는 Controller(`@Controller`)는 현재 `domain.admin`에만 존재하며, 별도로 `domain.admin.controller.web` 서브패키지에 위치시켜 REST Controller와 물리적으로 분리한다. 하나의 클래스가 두 역할을 동시에 하지 않는다.
- `controller`(REST API):
  - `ResponseEntity`를 사용해 응답한다.
  - Spring Security 인증 정보는 `@AuthenticationPrincipal`로 받는다.
  - 모든 API 메서드 상단에 `@LogMonitoring`을 붙인다(예외 없이 전 메서드 적용).
  - 각 Controller는 `XxxControllerDocs` interface를 implement한다. `ControllerDocs`는 Swagger 문서용으로, 프론트 개발자가 문서만 보고 개발할 수 있을 만큼 상세히 작성한다(`@Operation`, `@ApiResponse` 등). 별도 패키지를 만들지 않고 Controller와 동일 패키지(`controller`)에 위치시킨다.
  - `ControllerDocs` 인터페이스의 메서드 파라미터에는 `@Valid`, `@RequestBody`, `@RequestParam`, `@PathVariable`, `@AuthenticationPrincipal`, `@Parameter` 등 바인딩/문서용 어노테이션을 붙이지 않는다. Springdoc이 실제 구현체(`@Override`)의 어노테이션을 읽어 문서를 생성하므로 인터페이스에는 타입과 파라미터명만 선언한다.
  - 응답은 `success`/`data` 같은 공통 래퍼(envelope) 없이 DTO를 그대로 반환한다.
- `controller.web`(web MVC, 예: 관리자 Thymeleaf 페이지 — 현재 admin 도메인에만 존재): 뷰 이름(String)을 반환하는 순수 렌더링 담당. `ControllerDocs`/`@LogMonitoring`은 적용하지 않는다(REST API 전용 규칙).

## Swagger 문서화

목표는 "프론트 개발자가 Swagger 문서만 보고도 확실하게 개발이 가능한 수준"이다. `@Operation`/`@ApiResponse`의 설명 문장만으로는 부족하며, 아래를 전부 지킨다.

- **DTO 필드 단위 `@Schema` 필수**: Request/Response record의 모든 필드에 `@Schema(description = "...", example = "...")`를 붙인다. `description`은 그 필드가 무엇이고 어떤 제약이 있는지, `example`은 실제로 넣어볼 수 있는 값을 적는다.
- **Enum 필드는 가능한 값을 문장으로 명시**: Enum 타입 필드는 springdoc이 자동으로 `enum` 목록(예: `["GOOGLE", "APPLE"]`)을 스키마에 넣어주지만, 그 값들이 각각 무엇을 의미하는지는 자동으로 설명되지 않는다. `description`에 "GOOGLE 또는 APPLE만 허용된다"처럼 허용 값을 직접 문장으로 적는다.
  - 예시(`LoginRequest.socialType`): `@Schema(description = "소셜 로그인 제공자. GOOGLE 또는 APPLE만 허용된다.", example = "GOOGLE")`
- **에러 응답 스키마는 반드시 `content`로 연결**: `@ApiResponse`의 `description`에 에러 상황을 문장으로만 적으면 실제 응답 바디 구조(JSON 필드 구성)가 Swagger 문서에 전혀 나타나지 않는다(springdoc은 `content`가 연결된 스키마만 `components/schemas`에 포함시킨다). 에러 응답에는 항상 `content = @Content(schema = @Schema(implementation = ErrorResponse.class))`를 붙인다.
- **`@ApiResponse` description에 실제 발생 가능한 `errorCode` 값을 명시**: "검증 실패", "인증 실패" 같은 추상적인 설명 대신, 그 엔드포인트에서 실제로 던져질 수 있는 `ErrorCode` enum 값을 전부 나열한다. 하나의 HTTP 상태 코드에서 여러 `errorCode`가 나올 수 있으면(예: 401 하나에 `INVALID_TOKEN`/`REFRESH_TOKEN_NOT_FOUND`/`REFRESH_TOKEN_MISMATCH` 세 가지 모두 가능) 각각을 목록으로 구분해 적는다. 어떤 서비스 로직에서 어떤 `ErrorCode`를 던지는지는 실제 코드(`AuthService`, `GlobalExceptionHandler` 등)를 확인해 정확하게 작성하고, 추측하지 않는다.
  - 예시(`AuthControllerDocs.reissue`의 401 응답): `INVALID_TOKEN`(JWT 파싱 실패), `REFRESH_TOKEN_NOT_FOUND`(DB에 없음), `REFRESH_TOKEN_MISMATCH`(불일치/만료) 세 가지를 각각의 의미와 함께 명시.
- 작업 완료 후 실제로 `/v3/api-docs`(또는 `/docs/swagger-ui`)를 호출/조회해 의도한 필드 설명·example·enum 목록·에러 스키마가 실제 생성된 문서에 반영됐는지 확인한다(어노테이션 오타나 스코프 실수로 반영이 안 되는 경우가 있음).
- **API 변경 시 `@ApiChangeLogs`/`@ApiChangeLog`로 이력을 남긴다**: 엔드포인트의 요청/응답/문서 내용에 의미 있는 변경이 생길 때마다 해당 메서드에 항목을 추가한다(기존 항목은 지우지 않고 새 항목을 추가해 누적한다). Swagger 문서 하단에 날짜순 변경 이력 표로 자동 렌더링된다.
  - `date`: `yyyy-MM-dd` 형식(`chuseok22.api-change-log.date-format` 설정값과 일치해야 하며, 형식이 다르면 정렬 시 파싱 예외가 발생한다).
  - `author`: 문자열을 직접 쓰지 않고 `global/swagger/ChangeLogAuthor`에 정의된 상수를 참조한다(`author = ChangeLogAuthor.BAEK_JIHOON`). 새 작성자가 필요하면 이 클래스에 상수를 추가한다.
    - 왜 `enum`이 아니라 상수 클래스인가: `@ApiChangeLog(author = ...)`는 어노테이션 속성이라 Java 언어 규칙(JLS 9.7.1)상 컴파일 타임 상수 리터럴만 허용되고, `SomeEnum.CONST.name()`처럼 어떤 메서드 호출도 허용되지 않는다(직접 컴파일 테스트로 확인됨). 그래서 `SecurityPathConstants`와 동일하게 `public static final String` 상수를 쓰는 `final` 클래스로 관리한다.
  - `description`: 이번 변경으로 실제로 무엇이 바뀌었는지 한 줄로 적는다(무엇을 구현했는지가 아니라 "이번에 달라진 점" 위주).
  - `issueUrl`: 이 변경과 연결된 실제 GitHub 이슈 URL을 확인해서 적는다(추측 금지 — `gh issue view`로 실제 이슈 내용을 확인하고 해당 엔드포인트/기능과 관련 있는 이슈인지 검증한 뒤 연결한다). 이슈 없이(예: main 브랜치에서 직접) 작업한 경우에만 생략한다(기본값 빈 문자열).

## Common

- `Zone`처럼 전역적으로 설정할 값은 `global/config`에 Bean으로 등록해 재사용한다(`global/config/ClockConfig.java`의 `Clock` Bean, `Asia/Seoul` 고정). 각 파일 내부에서 `private static final ZoneId ...`처럼 개별 선언하지 않고, `Clock`을 생성자 주입받아 `LocalDateTime.now(clock)`으로 사용한다.
- Spring Security 경로 리터럴(permitAll 대상, securityMatcher 패턴, 로그인 페이지 등)은 `SecurityConfig` 내부에 직접 하드코딩하지 않고, `global/security/SecurityPathConstants`에 상수로 선언해 참조한다.
- `application.yml`은 수정 가능하며 git 추적 대상이다.
- `application-*.yml`(dev/prod)은 절대 수정 금지가 아니라 **git 추적 절대 금지**(내용 수정 자체는 필요 시 가능).
- 코드 포맷팅은 별도 도구(google-java-format, Checkstyle 등)를 도입하지 않고 IDE 기본 포맷터를 사용한다.

## Responsibility separation

- service / repository / controller 역할 기준: Controller(요청/응답) → Service(비즈니스 로직 전부) → Repository(데이터 접근만, 쿼리 메서드 외 로직 없음).
- 비즈니스 로직 위치 기준: 예외 없이 Service. Entity에는 로직을 두지 않는다(순수 데이터 홀더 + Builder/Setter).
- 화면 로직과 데이터 로직 분리 기준: 관리자 Thymeleaf 화면(`domain/admin`)의 뷰 렌더링 로직은 Controller에 최소한으로 두고, 실제 데이터 처리는 Service에 위임한다.

## Review expectations

- 리뷰 시 반드시 확인할 항목: Entity가 Builder+Setter 패턴을 따르는지, Controller가 비즈니스 로직을 포함하지 않는지, `getReferenceById` 미사용, DTO 검증 어노테이션 존재 여부, `ControllerDocs` 구현 여부, DTO 필드 `@Schema` 작성 여부 및 에러 응답의 `content`/`errorCode` 명시 여부(자세한 기준은 "Swagger 문서화" 참고)
- 성능 / 보안 / 유지보수 관점 체크리스트: N+1 쿼리 여부, 인증/인가 누락 여부(`@AuthenticationPrincipal` 또는 SecurityConfig 경로 설정), `application-*.yml` git 추적 여부
- 리뷰에서 block 걸어야 하는 기준: 비회원 접근이 가능한 회원 전용 API, `application-*.yml` 커밋 시도, Entity에 비즈니스 로직(도메인 메서드) 추가
