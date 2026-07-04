### 📌 작업 개요

ErrorCode 확장, ErrorResponse 확장, GlobalExceptionHandler 추가를 통해 인증 오류 및 유효성 검사 오류를 일관되게 처리할 수 있는 기반 마련.

### 🎯 구현 목표

Spring Boot 애플리케이션의 전역 예외 처리 구조를 강화하여:
1. 인증(AUTH) 관련 오류 코드 추가
2. 필드 유효성 검사 오류 상세 정보 지원
3. JWT 및 검증 예외를 일관된 ErrorResponse JSON으로 변환

### ✅ 구현 내용

#### ErrorCode 확장
- **파일**: `src/main/java/com/chuseok22/eodaegoserver/common/global/exception/ErrorCode.java`
- **변경 내용**: GLOBAL 섹션의 기존 3개 오류 코드 유지, AUTH 섹션에 5개 새 오류 코드 추가
  - UNAUTHORIZED(401): 인증이 필요한 경우
  - INVALID_TOKEN(401): 유효하지 않은 토큰
  - REFRESH_TOKEN_NOT_FOUND(401): 리프레시 토큰 미발견
  - REFRESH_TOKEN_MISMATCH(401): 리프레시 토큰 불일치 또는 만료
  - FIREBASE_TOKEN_VERIFICATION_FAILED(401): 소셜 로그인 토큰 검증 실패
- **이유**: 인증 및 토큰 관련 오류를 세분화하여 클라이언트에서 정확한 상황 파악 및 적절한 대응 처리 가능

#### ErrorResponse 확장
- **파일**: `src/main/java/com/chuseok22/eodaegoserver/common/global/exception/ErrorResponse.java`
- **변경 내용**: `List<FieldErrorDetail> fieldErrors` 필드 추가
- **이유**: Bean Validation 실패 시 필드별 검증 오류 상세 정보를 클라이언트에 전달하기 위함

#### FieldErrorDetail 생성
- **파일**: `src/main/java/com/chuseok22/eodaegoserver/common/global/exception/FieldErrorDetail.java`
- **변경 내용**: 필드명과 오류 메시지를 담는 레코드 타입 정의
- **이유**: 유효성 검사 오류 시 구체적인 필드 정보를 구조화된 형태로 제공

#### GlobalExceptionHandler 생성
- **파일**: `src/main/java/com/chuseok22/eodaegoserver/common/global/exception/GlobalExceptionHandler.java`
- **변경 내용**: 4개의 @ExceptionHandler 메서드 구현
  1. `handleCustomException()`: CustomException → ErrorCode의 상태 코드로 변환
  2. `handleValidationException()`: MethodArgumentNotValidException → INVALID_REQUEST + 필드 오류 상세 정보
  3. `handleJwtException()`: JwtException → INVALID_TOKEN
  4. `handleException()`: 그 외 모든 Exception → INTERNAL_SERVER_ERROR
- **basePackages**: `com.chuseok22.eodaegoserver.domain.auth`, `com.chuseok22.eodaegoserver.domain.member`로 스코핑
- **이유**: 통일된 JSON 응답 형식으로 예외를 처리하여 클라이언트 경험 개선 및 API 일관성 확보

### 🔧 주요 변경사항 상세

모든 예외는 `ResponseEntity<ErrorResponse>` 형태로 반환되며, 클라이언트는 일관된 구조의 오류 응답을 받습니다.

- MethodArgumentNotValidException 처리 시 BindingResult의 필드 오류를 FieldErrorDetail 레코드로 매핑하여 클라이언트에 상세 정보 제공
- JwtException(io.jsonwebtoken.JwtException)은 자동으로 INVALID_TOKEN으로 매핑
- 모든 기타 예외는 INTERNAL_SERVER_ERROR로 안전하게 처리

### 🧪 검증 결과

| 항목 | 결과 |
|------|------|
| 컴파일 | PASS (./gradlew compileJava BUILD SUCCESSFUL) |
| ErrorCode 오류 코드 | PASS (9개 상수, AUTH 섹션 추가) |
| ErrorResponse 필드 | PASS (errorCode, errorMessage, fieldErrors) |
| FieldErrorDetail | PASS (record 구현) |
| GlobalExceptionHandler | PASS (4개 @ExceptionHandler 메서드) |
| @RestControllerAdvice 스코핑 | PASS (auth, member 패키지) |

### ⚠️ 위험 요소

없음

### 📌 남은 이슈

Task 15(AuthController)에서 본 GlobalExceptionHandler가 사용될 예정.

---

**Commit**: `feat: ErrorCode 확장, ErrorResponse 확장, GlobalExceptionHandler 추가`
