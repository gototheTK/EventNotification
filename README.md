# 🗺️ 지역 행사 알림 안내 플랫폼 (Local Event Notification Platform)

사용자가 거주하거나 방문하는 지역의 다양한 문화행사 정보를 제공받고, 원하는 행사를 검색 및 '찜(Like)'할 수 있는 플랫폼입니다.
**현재 초기 단계(v1.0)로 서울시 공공데이터 API를 우선 연동**하여 서비스 중이며, 추후 전국 지자체 데이터로 확장할 수 있도록 유연성과 확장성을 고려하여 아키텍처를 설계했습니다. 대용량 트래픽에 대비한 **안정적인 데이터 파이프라인 구축**과 **보안(JWT) 중심의 설계**에 집중했습니다.

## 🚀 기술 스택 (Tech Stack)

### Backend
* **Language:** Kotlin 1.9+
* **Framework:** Spring Boot 3.x
* **Data Access:** Spring Data JPA, Hibernate
* **Database:** H2 (Local), MySQL (Production)
* **Security:** Spring Security, JWT (JSON Web Token)
* **Test:** JUnit5, Mockito

### Frontend
* **Core:** React 18, TypeScript, Vite
* **Styling:** Tailwind CSS, Lucide React
* **Network:** Axios

---

## ✨ 핵심 기능 (Key Features)

### 1. 🔄 확장 가능한 공공데이터 파이프라인 (Data Pipeline)
* **스케줄러 자동 동기화:** `@Scheduled`를 활용하여 매일 새벽 3시, 공공 API(현재 서울시 최신 데이터 우선 적용)를 데이터베이스로 안전하게 동기화합니다.
* **초기 데이터 적재 보장:** `ApplicationReadyEvent`를 활용하여 서버 초기 구동 시 DB가 비어있을 경우 자동으로 초기 데이터를 적재합니다.

### 2. 🔐 JWT 기반 인증 및 보안 (Security & JWT)
* **토큰 분리 전략:** Access Token과 Refresh Token의 Secret Key를 물리적으로 분리하여 탈취 시 피해 반경(Blast Radius)을 최소화했습니다.
* **Stateless 아키텍처:** 세션을 사용하지 않고(SessionCreationPolicy.STATELESS) Custom `OncePerRequestFilter`를 통해 매 요청마다 토큰을 검증합니다.

### 3. ❤️ 동시성을 고려한 '찜하기' 기능 (Event Like)
* **다대다(N:M) 연관관계 해소:** `Member`와 `Event` 사이의 다대다 관계를 `EventLike` 중간 엔티티로 승격하여 일대다-다대일 관계로 설계했습니다.
* **동시성 이슈 방어:** 사용자의 중복 요청(따닥 버튼 클릭 등)으로 인한 데이터 무결성 훼손을 막기 위해 DB 레벨에서 복합 유니크 제약조건(`UniqueConstraint`)을 적용했습니다.

### 4. 🔍 동적 검색 및 페이징 (Search & Pagination)
* **Spring Data JPA Specification:** Querydsl 등 무거운 라이브러리 없이, JPA Specification을 활용하여 제목, 카테고리, 지역구 등에 대한 안전하고 유연한 동적 쿼리를 구현했습니다.
* **성능 최적화:** `Pageable`을 적용하여 대량의 리스트 반환 시 네트워크 대역폭 낭비를 방지하고, 데이터 과적합(Over-fetching)을 막기 위해 엔티티를 List/Detail DTO로 철저히 분리했습니다.

### 5. 🛡️ 견고한 예외 처리 (Global Exception Handling)
* `@RestControllerAdvice`를 활용하여 비즈니스 로직에서 발생하는 맞춤형 예외(`EventNotFoundException` 등)를 전역으로 낚아채어, 프론트엔드에 일관된 에러 포맷(DTO)으로 응답하도록 설계했습니다.

---

## 🏗️ 아키텍처 및 개선 계획 (Architecture & Roadmap)

* **지역 확장 (Scale-out):** 현재 서울시 데이터에 국한된 파이프라인을 추후 OpenAPI 명세 표준화 작업을 통해 전국 지자체 단위 데이터로 확장할 예정입니다.
* **Redis Write-Back Pattern:** 향후 대규모 트래픽 발생 시, '찜하기' 요청의 DB 병목을 막기 위해 데이터를 **Redis**에 비동기적으로 먼저 적재하고, 스케줄러를 통해 DB로 일정 주기마다 동기화(Bulk Insert)하는 아키텍처로 진화시킬 계획입니다.
* **CI/CD 파이프라인:** Github Actions와 AWS EC2를 활용한 자동 배포 스크립트(.github/workflows)가 구성되어 있습니다.

---

## ⚙️ 실행 방법 (How to Run)

### Backend (Spring Boot)
1. `src/main/resources` 폴더에 `application-secret.yml` 파일을 생성하고 아래 환경 변수를 주입합니다.
   ```yaml
   open-api:
     seoul:
       key: "발급받은_서울시_API_키" # 초기 데이터 파이프라인용
   jwt:
     access-secret: "Base64_인코딩된_임의의_긴_문자열"
     refresh-secret: "Base64_인코딩된_다른_임의의_긴_문자열"


<img src="https://github.com/gototheTK/JavaSwingProject/blob/master/%EC%BA%A1%EC%B3%90%EC%98%81%EC%83%8112.gif?raw=true" width="800" height="400"> <br>
