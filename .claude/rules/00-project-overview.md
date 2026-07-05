# Project Overview

## Purpose

- 이 프로젝트의 목적: 서울어린이대공원 방문객이 AI 추천 코스를 참고해 공원을 탐방하고, 동식물·장소를 촬영/수집하며 도감을 완성하는 모바일 서비스 "어대GO"의 백엔드 서버
- 해결하려는 문제: 공원의 동식물·장소 정보를 쉽고 재미있게 제공하고, 촬영·퀴즈·수집을 결합해 능동적인 탐방 경험을 유도. 공식 데이터 기반의 신뢰할 수 있는 콘텐츠 제공
- 주요 사용자 또는 시스템 소비자: Flutter 모바일 앱(회원 전용, 비회원 기능 없음), 관리자(Thymeleaf 기반 웹 페이지 — 도감/퀴즈 콘텐츠 관리)

## Primary Stack

- Language: Java 21
- Framework: Spring Boot 4.x (Spring Security, Spring Data JPA, Thymeleaf)
- Runtime: JDK 21
- Database / storage: PostgreSQL 17 + PostGIS(위치 기반 기능용, 도입 예정), Redis(캐시/임시 데이터)
- Infra / deployment: Gradle 빌드, 외부 AI 추천 서버 연동, Firebase Admin SDK(소셜 로그인 토큰 검증)

## Important directories

- src/main/java/com/chuseok22/eodaegoserver/domain/: 도메인별 패키지(member, auth, admin 등). 각 도메인 내부에 entity, dto, repository, service, controller 위치
- src/main/java/com/chuseok22/eodaegoserver/global/: 도메인에 속하지 않는 전역 요소(config, properties, entity(BaseEntity), exception, security)
- src/main/resources/: application.yml(git 추적), application-{profile}.yml(git 추적 금지), templates/(관리자 Thymeleaf 뷰)
- docs/, .issue/, .report/: 개발 과정 산출물(스펙 문서, 이슈, 작업 보고서) — 자세한 내용은 40-delivery-and-review.md 참고

## Main commands

- install: `./gradlew build -x test`
- lint: 없음(별도 정적 분석 도구 미도입, IDE 기본 포맷터 사용)
- typecheck: 해당 없음(Java는 컴파일 시 타입 검증)
- build: `./gradlew build`
- unit-test / integration-test / e2e-test: 해당 없음 — 이 프로젝트는 테스트 코드를 작성하지 않는다 (자세한 내용은 30-testing-and-verification.md 참고)
- run-dev: `./gradlew bootRun --args='--spring.profiles.active=dev'`
- run-prod-like: `./gradlew bootRun --args='--spring.profiles.active=prod'`

## Project-specific constraints

- 반드시 지켜야 하는 제약:
  - `application.yml`은 수정 가능하며 git 추적 대상이다.
  - `application-*.yml`(dev/prod)은 절대 git에 추가하지 않는다. 실제 자격증명이 포함된 로컬/배포 전용 파일이다.
  - Firebase 서비스 계정 키(`firebase-adminsdk.json`)는 프로젝트 루트에 로컬로만 존재하며 git 추적 금지.
  - 비회원 기능은 제공하지 않는다 — 회원 관련 API는 항상 인증을 전제로 설계한다.
- 사용 금지 기술 / 패턴: 20-team-conventions.md 참고(Entity 팩토리 메서드 금지, getReferenceById 금지, 축약어 사용 금지 등)
- 현재 프로젝트에서 중요하게 보는 품질 기준: 명확한 계층 분리(Controller는 요청/응답만), 일관된 네이밍, Swagger 문서(ControllerDocs)를 통한 프론트 개발자와의 명확한 API 계약

## Change policy

- 어떤 변경은 허용되고 어떤 변경은 금지되는지: 기능 추가/버그 수정은 자유롭게 진행. 단, 이 문서의 규칙과 충돌하는 패턴을 새로 도입할 경우 반드시 사용자와 상의 후 진행
- 지금 레포에서 수정해도 되는 범위: `src/main/**`, `.claude/rules/**`, `CLAUDE.md`, `application.yml`
- 절대 건드리면 안 되는 영역: `application-*.yml`(내용 수정 자체는 필요시 가능하나 git 추적 금지), `firebase-adminsdk.json` 및 기타 자격증명 파일, 사용자가 명시적으로 요청하지 않은 커밋/푸시
