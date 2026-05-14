# 🗺️ 지역 행사 알림 플랫폼 (Event Notification Platform)

서울시 공공데이터 API를 기반으로 문화행사 정보를 수집·제공하고, 사용자가 행사를 검색하거나 '찜(Like)'할 수 있는 플랫폼입니다.
도메인 중심 패키지 구조, PostGIS 기반 위치 검색, Redis 동시성 제어, JWT 보안을 핵심 설계 원칙으로 삼았습니다.

---

## 🚀 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Kotlin 1.9+ |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL 14+ + PostGIS |
| ORM | Spring Data JPA, Hibernate 6 + hibernate-spatial |
| Cache / 동시성 | Redis (Spring Data Redis, `StringRedisTemplate`) |
| 위치 데이터 | JTS (Java Topology Suite), SRID 4326 |
| Security | Spring Security 6, JWT (jjwt 0.11) |
| Test | JUnit5, Mockito, MockMvc |

---

## 📦 패키지 구조

```
org.service.event
├── EventApplication.kt          # 진입점 (@EnableScheduling 포함)
├── global/                      # 전역 인프라 (Security, JWT, 예외)
│   ├── SecurityConfig.kt
│   ├── JwtTokenProvider.kt
│   ├── JwtAuthenticationFilter.kt
│   ├── GlobalExceptionHandler.kt
│   └── CustomExceptions.kt
├── domain/
│   ├── event/                   # 행사 도메인
│   │   ├── Event.kt             # Entity + PromotedTier enum + JTS Point
│   │   ├── EventRepository.kt   # JpaRepository + ST_DWithin 네이티브 쿼리
│   │   ├── EventService.kt
│   │   ├── EventController.kt
│   │   ├── EventResponse.kt     # EventListResponse, EventDetailResponse
│   │   └── EventSpecification.kt
│   ├── like/                    # 찜하기 도메인
│   │   ├── EventLike.kt
│   │   ├── EventLikeRepository.kt  # Fetch Join으로 N+1 해결
│   │   ├── EventLikeService.kt     # Redis 1차 방어 + DB 2차 방어
│   │   └── EventLikeController.kt
│   └── member/                  # 회원 도메인
│       ├── Member.kt
│       ├── MemberRepository.kt
│       └── Role.kt
└── infrastructure/
    └── sync/                    # 외부 API 동기화
        ├── EventSyncService.kt  # 네트워크·트랜잭션 분리, Batch Insert
        ├── EventSyncDto.kt
        └── InitialDataLoader.kt
```

---

## ✨ 핵심 기능

### 1. PostGIS 기반 위치 검색

`EventLocation`의 위경도를 JTS `Point` (SRID 4326) 타입으로 저장합니다.
`EventRepository`에 PostGIS `ST_DWithin`을 활용한 반경 검색 네이티브 쿼리를 구현하여,
현재 위치 기준 N미터 이내 행사를 조회할 수 있습니다.

```
GET /api/events/nearby?latitude=37.5&longitude=127.0&radiusMeters=1000
```

### 2. 수익화 기반 — PromotedTier

`Event` 엔티티에 `PromotedTier` (NONE / BRONZE / SILVER / GOLD) 필드를 도입하여
랭킹 기반 광고 노출 시스템의 뼈대를 마련했습니다.

### 3. 확장 가능한 공공데이터 파이프라인

- **스케줄러 자동 동기화:** 매일 새벽 3시, 서울시 문화행사 API를 자동 동기화합니다.
- **초기 데이터 자동 적재:** `ApplicationReadyEvent`로 서버 첫 구동 시 DB가 비어 있으면 즉시 데이터를 적재합니다.
- **네트워크 · 트랜잭션 분리:** 메서드에서 `@Transactional`을 제거하고, 긴 HTTP 대기 구간 동안 DB 커넥션을 점유하지 않습니다. DB 쓰기는 `saveAll()` 단일 Batch INSERT로 처리합니다.
- **날짜 파싱 안전성:** 날짜 파싱 실패 시 `LocalDateTime.now()` 폴백 대신 해당 행 전체를 스킵하여 `startDate > endDate` 역전 구간이 DB에 저장되는 오염을 방지합니다.

### 4. JWT 기반 인증 · 보안

- Access Token과 Refresh Token의 Secret Key를 분리하여 탈취 시 피해를 최소화합니다.
- JWT `subject`에 이메일을 저장하며, `getAuthentication()`에서 `role` 클레임 누락 시 묵음 실패 대신 명시적 예외를 발생시킵니다.
- 필터에서 `getAuthentication()` 예외를 `runCatching`으로 포착하여, 불완전한 JWT를 500이 아닌 미인증 처리(401)로 안전하게 격하합니다.
- CORS 허용 Origin 등 환경별 설정값은 `application.yaml`의 커스텀 속성으로 외부화했습니다.

### 5. Redis 기반 동시성 제어 — '찜하기'

Redis `SADD`는 원자적으로 동작합니다. '따닥 클릭' 같은 동시 요청을 메모리 레벨에서 1차 차단합니다.

```
① Redis SADD(key, email)
   → 반환 0 (이미 존재) : DuplicateLikeException 즉시 발생 (DB 쿼리 없음)
   → 반환 1 (신규 추가) : DB 처리로 진행

② DB 2차 방어
   → 성공 : 찜하기 완료
   → MemberNotFoundException / EventNotFoundException
      : Redis 항목 롤백 후 재전파 (캐시 일관성 유지)
   → DataIntegrityViolationException (경쟁 조건)
      : DuplicateLikeException 변환 (Redis 항목 유지)
```

Key 형식: `event:{eventId}:likes`

### 6. 동적 검색 · 페이징

`EventSpecification`으로 제목 / 카테고리 / 지역구 조건을 동적으로 조합합니다.
제목 검색의 LIKE 와일드카드(`%`, `_`, `\`)를 이스케이프하여
의도치 않은 대량 매칭과 인덱스 무력화를 방지합니다.

### 7. 견고한 예외 처리

`@RestControllerAdvice`로 모든 커스텀 예외를 전역 포착하여 일관된 에러 포맷으로 응답합니다.

---

## ⚙️ 실행 방법

### 사전 요구사항

- PostgreSQL 14+ with PostGIS extension
- Redis 6+

```sql
-- PostgreSQL에서 PostGIS 활성화
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 환경 변수 설정

`src/main/resources/secret/application-secret.yml` 파일을 생성합니다.

```yaml
open-api:
  seoul:
    key: "발급받은_서울시_API_키"

jwt:
  access-secret: "Base64_인코딩된_액세스_시크릿_키"
  refresh-secret: "Base64_인코딩된_리프레시_시크릿_키"
```

### application.yaml 주요 설정 (`local` 프로파일)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eventdb
  data:
    redis:
      host: localhost
      port: 6379

app:
  cors:
    allowed-origins: "http://localhost:5173"

api:
  seoul:
    base-url: "http://openapi.seoul.go.kr:8088"
```

### 서버 실행

```bash
# local 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 🔑 주요 API 엔드포인트

| Method | URL | 설명 | 인증 |
|---|---|---|---|
| GET | `/api/events` | 행사 목록 조회 (검색·페이징) | 불필요 |
| GET | `/api/events/{id}` | 행사 상세 조회 | 불필요 |
| GET | `/api/events/nearby` | 반경 N미터 행사 조회 (PostGIS) | 불필요 |
| POST | `/api/events/{id}/likes` | 행사 찜하기 | JWT 필요 |
| DELETE | `/api/events/{id}/likes` | 찜 취소 | JWT 필요 |
| GET | `/api/test/sync-events` | 수동 데이터 동기화 | 불필요 |
