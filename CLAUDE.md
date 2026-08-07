# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 & 실행 명령어

```bash
./gradlew build          # 프로젝트 빌드 (결과물: build/libs/gamja-1.0.0.jar)
./gradlew bootRun        # 로컬 실행 (기본 포트: 58080)
./gradlew test           # 전체 테스트 실행
./gradlew test --tests "com.phobi.gamja.SomeTest"  # 단일 테스트 클래스 실행
```

Docker:
```bash
docker-compose up        # docker-compose 실행 (외부 포트: 51888 → 내부: 58080)
```

DB: `localhost:3306` MariaDB, 데이터베이스명 `gamja`. `PORT` 환경변수로 서버 포트(기본값 58080) 변경 가능.

## 아키텍처 개요

**Gamja**는 브라우저 기반 RPG 게임 서버다. Spring Boot REST API + 바닐라 JS 프론트엔드 (프론트 빌드 과정 없음).

### 레이어드 아키텍처

```
Controller → Service → Repository → Entity
```

- **Controller** (`controller/`): `@RestController`, `ResponseEntity<GamJaResponse>` 반환. 전부 `@RequiredArgsConstructor` (Lombok) 사용.
- **Service** (`service/`): `@Transactional` 비즈니스 로직. 배틀, 보스, 가챠, 강화, 연금, 퀘스트, 업적, 명성 등 게임 시스템 포함.
- **Repository** (`repository/`): Spring Data JPA 인터페이스. 도메인별 하위 디렉토리로 구성 (`user/`, `battle/`, `dex/`, `item/`, `achievement/` 등).
- **Entity** (`entity/`): Lombok 적용 JPA 엔티티. 복합 PK는 `@EmbeddedId` 사용. 12개 도메인에 걸쳐 88개 엔티티.
- **DTO** (`dto/`): 도메인별 요청/응답 객체.

### 표준 API 응답 형식

모든 엔드포인트는 `GamJaResponse`를 반환한다:
- `code`: `SUCCESS`, `FAIL`, `OK`, `ERROR`
- `message`: 사람이 읽을 수 있는 메시지
- `data`: 페이로드 객체

### 보안

- 세션 기반 인증 (Spring Security 미사용) — 커스텀 `UserAgentInterceptor`로 세션 검증
- `@SanitizeInput` 어노테이션 + `InputSanitizerAspect`로 AOP 기반 XSS 방어
- `GlobalExceptionHandler` (`@RestControllerAdvice`)로 예외 일괄 처리
- 인증 엔드포인트는 인터셉터 제외, 나머지는 모두 유효한 세션 필요

### 프론트엔드

정적 파일 위치: `src/main/resources/static/`. 바닐라 JS + CSS, 번들러 없음. `char.html`이 메인 게임 화면. `playerInfo.js`가 페이지 간 공유 플레이어 상태 관리.

### 주요 도메인 개념

| 도메인 | 설명 |
|--------|------|
| Dex | 캐릭터/몬스터 도감 (포켓덱스 방식) |
| UserCorps | 현재 편성된 파티 |
| Battle | 몬스터와의 자동 전투 루프 |
| BossBattle | 패턴이 있는 보스 전투 |
| Achievement | `RequirementType` enum으로 조건 추적 |
| Fame | 랭킹/티어 시스템 |
| Chronicle | 활동 로그 |
| Skin | 테두리·배경 등 코스메틱 |

### XP & 스탯

`XpConfig.java`에 게임 상수 정의. `StatCalculator.java`에서 파생 스탯 계산 처리.

### JPA 주의사항

- `ddl-auto: update` — 서버 기동 시 스키마 자동 변경됨. 컬럼명 변경 시 기존 컬럼은 남고 새 컬럼이 추가되므로 주의.
- SQL 로깅 활성화 (`show-sql: true`) — N+1 쿼리 디버깅에 활용.
- `@ManyToOne`/`@OneToMany`는 명시적으로 필요한 경우가 아니면 기본 지연 로딩(lazy) 유지.
