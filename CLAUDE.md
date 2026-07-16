# CLAUDE.md

어대GO(eodaego-server) 백엔드 프로젝트의 Claude Code 작업 규칙입니다. 이 파일은 진입점이며, 상세 규칙은 `.claude/rules/*.md`에 있습니다. 작업 시작 전 반드시 아래 파일들을 읽고 이해한 뒤 진행하세요.

- [`.claude/rules/00-project-overview.md`](.claude/rules/00-project-overview.md) — 프로젝트 목적, 스택, 주요 디렉토리, 명령어, 제약사항
- [`.claude/rules/10-architecture-and-boundaries.md`](.claude/rules/10-architecture-and-boundaries.md) — 아키텍처, 모듈 경계, 데이터 흐름, 폴더 규칙
- [`.claude/rules/20-team-conventions.md`](.claude/rules/20-team-conventions.md) — 네이밍, Entity/DTO/Repository/Service/Controller 코딩 컨벤션
- [`.claude/rules/30-testing-and-verification.md`](.claude/rules/30-testing-and-verification.md) — 테스트 정책(이 프로젝트는 테스트 코드를 작성하지 않음), 검증 명령
- [`.claude/rules/40-delivery-and-review.md`](.claude/rules/40-delivery-and-review.md) — 보고서 형식, PR 규칙, 배포 전 확인사항

## 핵심 요약 (전체 내용은 위 rules 파일 참고)

- **패키지 구조**: `domain.<도메인>`은 `entity`/`repository`/`service`/`controller`/`dto.request`/`dto.response` 서브패키지로 세분화한다(실제 존재하는 계층만, 기준 패턴은 `domain/member` 참고). `controller`는 REST API(`@RestController`) 전용이며, web MVC(`@Controller`)는 현재 `domain.admin`에만 존재하므로 `domain.admin.controller.web` 서브패키지에 위치한다. `global.*`(config/properties/entity/exception/security)은 도메인에 속하지 않는 공통 요소 전용이며, `@ConfigurationProperties`는 도메인 전용 데이터여도 예외 없이 `global/properties/`에 둔다. `common` 접두사는 사용하지 않는다.
- **Entity**: `@Getter @Setter @Builder`. 생성은 `@Builder`로 통일(정적 팩토리 메서드 `create()`/`register()` 등은 금지). 변경은 Setter 또는 관련 필드를 묶어 처리하는 도메인 메서드(`update()`, `updateStatus()` 등 단순 필드 대입만 수행) 모두 허용하며, 조건 분기·외부 호출이 섞인 복잡한 비즈니스 로직은 Entity에 두지 않고 Service로 위임한다. `BaseEntity` 상속, PK는 UUID, 연관관계는 JPA 어노테이션, Enum은 STRING, boolean 필드는 `is` 접두사 없이.
- **DTO**: record + Bean Validation, `dto/request`·`dto/response`로 분리(신규 작성분부터).
- **Repository**: `getReferenceById` 금지, `findById` 사용. JPQL 지양.
- **Service**: 모든 비즈니스 로직 위치. `@Slf4j` + 레벨 기준 준수. `@RequiredArgsConstructor`.
- **Controller**: `controller` 패키지는 REST API(`@RestController`) 전용이다. `ResponseEntity`, `@AuthenticationPrincipal`, 모든 메서드에 `@LogMonitoring`, `XxxControllerDocs` interface 구현, 응답 래퍼(envelope) 없이 데이터 그대로 반환. Web MVC(`@Controller`, 세션 기반 페이지 렌더링)는 `domain.admin.controller.web`에만 존재하며, 뷰 이름만 반환하는 순수 렌더링 담당(`@LogMonitoring`/`ControllerDocs` 미적용).
- **테스트**: 이 프로젝트는 테스트 코드를 작성하지 않는다(명시적 정책). 검증은 `./gradlew compileJava`/`build`와 수동 스모크 테스트로 대신한다.
- **절대 금지**: `application-*.yml`(dev/prod) git 커밋, `firebase-adminsdk.json` 등 자격증명 커밋, 축약어 사용, Entity에 정적 팩토리 메서드나 조건 분기·외부 호출이 섞인 복잡한 비즈니스 로직 추가(단순 필드 대입 도메인 메서드는 허용), `application.yml`의 `admin`/`jwt`/`firebase` 관련 값은 실제 값이 아닌 예시 값 그대로 커밋.

## 참고: Clock/시간 처리

전역 시간대는 `global/config/ClockConfig.java`의 `Clock` Bean(`Asia/Seoul`)으로 통일한다. 개별 파일에서 `ZoneId`/`LocalDateTime.now()`를 직접 선언하지 않고 `Clock`을 생성자 주입받아 `LocalDateTime.now(clock)`으로 사용한다.
