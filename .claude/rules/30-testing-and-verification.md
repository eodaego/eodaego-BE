# Testing And Verification

## Test strategy

- 이 프로젝트의 기본 테스트 전략: **테스트 코드를 작성하지 않는다.** 사용자의 명시적 결정이며, 글로벌 CLAUDE.md의 TDD 기본 방침보다 이 프로젝트 규칙이 우선한다.
- 단위 / 통합 / E2E 우선순위: 해당 없음(테스트 코드 미작성).
- mock 사용 기준: 해당 없음.
- 회귀 테스트 기준: 코드 작성 후 `./gradlew compileJava` 통과와, 필요 시 `./gradlew bootRun`을 통한 수동 스모크 테스트로 대체한다.

## Commands

- 최소 검증 명령: `./gradlew compileJava`
- 구현 후 반드시 실행해야 하는 명령: `./gradlew compileJava` (컴파일 성공 확인)
- PR 전 반드시 실행해야 하는 명령: `./gradlew build` (단, 기존 `EodaegoServerApplicationTests.contextLoads()`는 `firebase-adminsdk.json`이 로컬에 있어야 통과 — 없으면 실패는 예상된 결과이며 코드 결함이 아님)
- 일부 모듈만 대상으로 빠르게 검증하는 명령: `./gradlew compileJava` (전체 컴파일 외 모듈 단위 분리 없음)

## Evidence

- 테스트 성공/실패를 어떻게 기록할지: 테스트 코드가 없으므로 `./gradlew compileJava`/`build` 명령 실행 결과(BUILD SUCCESSFUL 여부)를 근거로 기록한다.
- 스크린샷 / 로그 / 요약 결과 작성 방식: 수동 스모크 테스트 시 실행한 curl/브라우저 요청과 응답 결과를 보고서(`.report/`)에 요약.
- UI 작업 시 필요한 검증 산출물: 관리자 Thymeleaf 페이지 작업 시 실제 브라우저에서 로그인/로그아웃 흐름을 수동 확인한 결과를 기록.

## Failure handling

- 실패 시 우선 확인할 것: 컴파일 에러 메시지, `application-*.yml`/`firebase-adminsdk.json` 등 로컬 자격증명 파일 존재 여부(누락이 원인인 경우가 많음).
- flaky 판단 기준: 해당 없음(자동화된 테스트가 없어 flaky 테스트 개념도 없음).
- 실패 결과 분석 시 필요한 로그 위치: `build/reports/tests/test/index.html`(전체 빌드 시 남는 리포트), 애플리케이션 기동 로그(콘솔 출력).
