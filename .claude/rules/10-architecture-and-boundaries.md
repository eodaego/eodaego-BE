# Architecture And Boundaries

## High-level architecture

- 현재 시스템의 상위 구조: Spring Boot 단일 모놀리식 서버. `/api/{version}/**`(회원용 REST API, JWT 인증)와 `/admin/**`(관리자 웹, 세션 기반 인증)의 두 인증 채널이 완전히 분리된 SecurityFilterChain으로 공존한다.
- 주요 계층: Controller(요청/응답) → Service(비즈니스 로직) → Repository(데이터 접근) → Entity(도메인 모델). DTO는 Controller-Service 경계에서만 사용하고 Entity를 외부로 노출하지 않는다.
- 외부 시스템 연동: Firebase Admin SDK(Google/Apple 소셜 로그인 ID Token 검증), 외부 AI 추천 서버(코스 추천), PostgreSQL(PostGIS 포함), Redis

## Module boundaries

- 각 주요 모듈의 책임: `domain.<도메인>`은 해당 도메인의 entity/dto/repository/service/controller를 전부 소유하며, 이 각각을 **서브패키지로 세분화**한다(`domain.<도메인>.entity`, `.repository`, `.service`, `.controller`, `.dto.request`, `.dto.response`). 도메인 안에 특정 계층이 실제로 존재하지 않으면 해당 서브패키지는 만들지 않는다(예: `domain.member`는 아직 service/controller가 없으므로 `entity`, `repository`만 존재). Enum처럼 entity/dto/repository/service/controller 어디에도 속하지 않는 보조 타입은 도메인 루트에 둔다(예: `domain.member.SocialType`).
- `controller`는 REST API(`@RestController`, JSON API) 전용 패키지다. Web MVC(`@Controller`, 세션/Thymeleaf)를 반환하는 Controller는 현재 `domain.admin`에만 존재하며, `domain.admin.controller.web` 서브패키지에 물리적으로 분리해 위치시킨다(예: 추후 `domain.admin`에 관리자용 REST API가 추가되면 `domain.admin.controller`에 위치, 기존 MVC 페이지는 계속 `domain.admin.controller.web`에 유지). `ControllerDocs` 인터페이스는 REST `controller`에만 존재한다(REST API의 Swagger 문서용이므로 MVC `controller.web`에는 불필요).
- `global.*`은 특정 도메인에 속하지 않는 공통 설정(config), 공통 프로퍼티(properties), 공통 엔티티(entity — BaseEntity), 예외 처리(exception), 보안(security)만 둔다. `@ConfigurationProperties` 레코드는 도메인 전용 데이터를 담고 있어도 예외 없이 `global/properties/`에 둔다(예: 관리자 계정 목록인 `AdminAccountProperties`도 `domain.admin`이 아닌 `global.properties`에 위치).
- 모듈 간 허용 의존 방향: 도메인 패키지 간(예: `domain.auth` → `domain.member`) 자유로운 참조를 허용한다. 모든 도메인은 `global.*`을 참조할 수 있다. `global.*`은 특정 `domain.*`을 참조하지 않는다(역방향 의존 금지).
- 금지 의존 관계: 도메인 패키지 간 순환 참조 금지(예: `domain.member`가 다시 `domain.auth`를 참조하는 구조 금지). `global.*` → `domain.*` 방향의 의존 금지.
- `domain.admin`의 외부 도메인 참조 원칙: 다른 도메인의 Entity/Repository는 필요시 직접 참조할 수 있지만, 다른 도메인의 Service 메서드는 재사용하지 않는다. 일반 사용자 대상 Service는 status/활성화 여부에 따라 조회 결과를 제한하는 필터링 로직을 포함하는 경우가 많은데, 관리자는 상태와 무관하게 전체 데이터를 조회·관리해야 하므로 이런 필터링 로직을 그대로 물려받으면 안 된다. 관리자 전용 조회/변경 로직은 `domain.admin.service`에 별도로 작성한다(필요하면 Repository에 관리자 전용 쿼리 메서드를 추가).

## Data flow

- 요청/응답 흐름: Controller가 `@Valid @RequestBody`로 DTO(record)를 받아 Service에 그대로 전달 → Service가 Entity 조회/생성/변경 후 응답 DTO(record)로 변환해 반환 → Controller는 `ResponseEntity`로 감싸서 반환. 응답은 별도 envelope(success/data 래퍼) 없이 DTO를 그대로 반환한다.
- 비동기 처리 흐름: 현재 없음. 도입 시 이 문서에 추가.
- 캐시/큐/스토리지 사용 방식: Redis는 캐시/임시 데이터 용도로 사용(구체적 캐시 전략은 기능 추가 시 확정). PostgreSQL이 주 저장소이며 PostGIS로 위치 기반 쿼리를 처리할 예정.

## File / folder conventions

- 폴더 구조 규칙:
  - `domain/<도메인명>/entity/`: JPA Entity
  - `domain/<도메인명>/repository/`: Spring Data JPA Repository
  - `domain/<도메인명>/service/`: 비즈니스 로직(`XxxService`, 그 외 도메인 전용 컴포넌트도 여기 포함 — 예: `FirebaseTokenVerifier`, `AdminAccountInitializer`)
  - `domain/<도메인명>/controller/`: `@RestController`(JSON REST API) 전용. `XxxController.java`, `XxxControllerDocs.java`(Swagger 문서, 동일 패키지)
  - `domain/admin/controller/web/`: `@Controller`(web MVC, 세션 기반 페이지 렌더링) 전용 — 현재 `domain.admin`에만 존재
  - `domain/<도메인명>/dto/request/`, `domain/<도메인명>/dto/response/`: 요청/응답 DTO(record) — REST `controller`에서만 사용
  - `domain/<도메인명>/`(루트): 위 다섯 계층 어디에도 속하지 않는 보조 타입(Enum 등, 예: `SocialType`)
  - `global/config/`: `@Configuration` 클래스(예: `SecurityConfig`, `ApiVersionConfig`, `ClockConfig`, `FirebaseConfig`, `SwaggerConfig`)
  - `global/properties/`: 모든 `@ConfigurationProperties` 클래스. 도메인 전용 데이터를 담더라도 예외 없이 여기 위치(`JwtProperties`, `FirebaseProperties`, `AdminAccountProperties`, `SpringDocProperties`)
  - `global/entity/`: `BaseEntity`(모든 Entity가 상속하는 공통 Auditing 필드)
  - `global/exception/`: `ErrorCode`, `CustomException`, `ErrorResponse`, `FieldErrorDetail`, `GlobalExceptionHandler`
  - `global/security/`: `Role`, `SecurityPathConstants` 등 순수 Spring Security 로직(설정값이 아닌 것). JWT 발급/검증/필터/핸들러는 `global/security/jwt/` 하위(`JwtProvider`, `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`) — 단, `JwtProperties`는 위 규칙에 따라 `global/properties/`에 위치
- 새 파일 생성 시 위치 기준: 특정 도메인에 속한 로직이면 `domain.<도메인>.<계층>` 하위에, 여러 도메인이 공유하거나 도메인에 속하지 않는 설정/공통 처리라면 `global.*` 하위에 둔다. 도메인 내부에서도 반드시 entity/repository/service/controller/dto 계층별 서브패키지에 위치시키며, 도메인 루트에 클래스를 직접 두지 않는다(Enum 등 계층에 속하지 않는 보조 타입 제외).
- 공통 유틸 / 도메인 로직 / API 계층 분리 기준: 비즈니스 로직은 반드시 Service에 위치. Controller는 요청 바인딩과 응답 반환만 수행하며 분기/변환/조건문을 포함하지 않는다.

## Extension points

- 기능 추가 시 먼저 참고해야 하는 패턴: `domain/auth`(Member 도메인 REST API + JWT), `domain/admin`(세션 기반 관리자 페이지) — 두 패턴 모두 이번 Spring Security 이슈에서 확립됨. 도메인 내부 서브패키지 분리는 `domain/member`가 기준 패턴.
- 기존 구현 재사용 포인트: `global/exception`의 `ErrorCode`/`CustomException`/`ErrorResponse`/`GlobalExceptionHandler` 패턴을 새 도메인 예외 처리에도 그대로 재사용. `GlobalExceptionHandler`는 `@RestControllerAdvice(annotations = RestController.class)`로 스코프되어 `@RestController`(JSON API)에서 발생한 예외만 처리하며, `@Controller`(관리자 Thymeleaf MVC)는 자동으로 제외된다 — 새 도메인이 추가돼도 별도 설정 없이 자동 적용된다.
- 대표적으로 따라야 하는 파일 경로 예시: `domain/member/entity/Member.java`(Entity), `domain/member/repository/MemberRepository.java`(Repository), `domain/auth/service/AuthService.java`(Service), `domain/auth/controller/AuthController.java`(REST Controller), `domain/auth/controller/AuthControllerDocs.java`(Swagger 문서), `domain/auth/dto/request/LoginRequest.java`(Request DTO), `domain/admin/controller/web/AdminLoginController.java`(MVC Controller), `global/properties/JwtProperties.java`(설정값), `global/exception/GlobalExceptionHandler.java`(예외 처리)
