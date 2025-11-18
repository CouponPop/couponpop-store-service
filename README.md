# <img width="50" height="50" alt="image" src="https://github.com/user-attachments/assets/dd2d9522-3591-454c-9cd2-0b534b75b8b9" /> CouponPop Store Service

**CouponPop 플랫폼의 매장 관리 마이크로서비스**

매장 등록, 조회, 관리 및 고급 검색 기능을 제공하는 Spring Boot 기반 RESTful API 서비스입니다.

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.11-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.17.4-005571?style=flat&logo=elasticsearch)](https://www.elastic.co/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql)](https://www.mysql.com/)
[![OpenAI](https://img.shields.io/badge/OpenAI-API-412991?style=flat&logo=openai)](https://openai.com/)

---

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [시작하기](#-시작하기)
- [API 엔드포인트](#-api-엔드포인트)
- [고급 검색 시스템](#-고급-검색-시스템)
- [데이터베이스 구조](#-데이터베이스-구조)
- [환경 설정](#-환경-설정)
- [테스트](#-테스트)
- [Health Check](#health-check)
- [모니터링](#-모니터링)

---

## ✨ 주요 기능

### 🏢 매장 관리
- **CRUD 작업**: 매장 생성, 조회, 수정, 삭제 (소프트 삭제)
- **소유자 인증**: JWT 기반 매장 소유권 검증
- **다중 매장 지원**: 한 소유자가 여러 매장 운영 가능
- **매장 카테고리**: 카페, 음식점, 편의점 등 분류

### 🗺️ 위치 기반 서비스
- **지리 공간 검색**: geo_point를 활용한 반경 기반 매장 검색
- **거리 계산**: 사용자 위치에서 매장까지의 거리 자동 계산

### 🔍 고급 검색 시스템
- **하이브리드 검색**: BM25(키워드) + KNN(시맨틱) 결합
- **OpenAI Embeddings**: text-embedding-3-small 모델 활용 (1536차원)
- **다양한 검색 모드**:
  - 하이브리드 검색 (키워드 + 의미론)
  - 순수 시맨틱 검색 (벡터 유사도)
  - 키워드 검색 (BM25)
  - 자동완성 (Suggest)
- **N-gram 분석**: 부분 매칭 및 오타 허용
- **검색 가중치**: 매장명(3.0), 주소(1.5), 설명(1.0)

### 🔄 데이터 동기화
- **자동 인덱싱**: 매장 생성/수정 시 Elasticsearch 자동 동기화
- **벡터 생성**: OpenAI API를 통한 임베딩 벡터 자동 생성
- **배치 재인덱싱**: 기존 데이터 일괄 처리 지원

### 🔗 내부 서비스 통신
- **OpenFeign**: 다른 마이크로서비스와의 통신
- **Internal API**: CouponEvent 서비스 등과의 데이터 공유
- **소유권 검증**: 타 서비스에서 매장 소유권 확인
- **커서 기반 페이징**: 효율적인 대량 데이터 조회

### 📊 운영 및 모니터링
- **Spring Actuator**: Health check, Metrics
- **Prometheus**: 메트릭 수집 및 모니터링
- **Jacoco**: 코드 커버리지 측정
- **ELK Stack**: 로그 수집 및 분석 (Elasticsearch, Logstash, Kibana)
- **Filebeat**: 로그 파일 수집 및 전송

---

## 🛠 기술 스택

### Backend Framework
- **Java 17**: LTS 버전의 Java
- **Spring Boot 3.4.11**: 최신 Spring Boot
- **Spring Data JPA**: ORM 및 데이터 접근 계층
- **Spring Security**: JWT 기반 인증/인가
- **QueryDSL**: 타입 안전한 쿼리 작성

### Database
- **MySQL 8.0**: 메인 데이터베이스
- **Flyway**: 데이터베이스 마이그레이션
- **Hibernate Spatial**: 지리 공간 데이터 처리
- **Master-Slave Replication**: 읽기/쓰기 분산

### Search & AI
- **Elasticsearch 8.x**: 전문 검색 엔진
- **OpenAI API**: 텍스트 임베딩 생성
- **Spring Data Elasticsearch**: Elasticsearch 통합

### Infrastructure & DevOps
- **Docker**: 컨테이너화
- **Jenkins**: CI/CD 파이프라인
- **AWS Parameter Store**: 환경 변수 관리
- **Gradle**: 빌드 도구

### Monitoring & Logging
- **Micrometer + Prometheus**: 메트릭 수집
- **ELK Stack**: 로그 분석
- **Filebeat**: 로그 수집기
- **Logback**: 로그 관리

### Testing
- **JUnit 5**: 단위 테스트
- **Testcontainers**: 통합 테스트
- **Spring Security Test**: 보안 테스트
- **H2 Database**: 테스트용 인메모리 DB

---

## 🏗 시스템 아키텍처

### 레이어 아키텍처

<img width="1173" height="571" alt="image" src="https://github.com/user-attachments/assets/81ee8316-e000-4671-8669-e1e543da3352" />

### 데이터 흐름

#### 1. 매장 생성 플로우

<img width="1330" height="624" alt="image" src="https://github.com/user-attachments/assets/89873486-06bc-426e-be42-d82d67e0ffeb" />

#### 2. 하이브리드 검색 플로우

<img width="821" height="632" alt="image" src="https://github.com/user-attachments/assets/54430f47-ff26-4f51-8968-fcc8bee1a63d" />

---

## 🚀 시작하기

### 사전 요구사항

- **Java 17** 이상
- **MySQL 8.0** 이상
- **Elasticsearch 8.17.4**
- **OpenAI API Key**
- **Docker** (선택사항)
- **Gradle 8.7.0** (Wrapper 포함)

### 환경 설정

#### 1. 저장소 클론

```bash
git clone https://github.com/CouponPop/couponpop-store-service.git
cd couponpop-store-service
```

#### 2. 환경 변수 설정

`.env` 파일 생성:

```bash
# Database
DB_MASTER_URL=jdbc:mysql://localhost:3306/store_db
DB_SLAVE_URL=jdbc:mysql://localhost:3306/store_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Elasticsearch
ELASTICSEARCH_URI=http://localhost:9200

# OpenAI
OPENAI_API_KEY=sk-your-openai-api-key

# JWT
JWT_SECRET_KEY=your-jwt-secret-key-here

# GitHub Packages (의존성 다운로드용)
GITHUB_ACTOR=your-github-username
GITHUB_TOKEN=your-github-token
```

#### 3. 데이터베이스 설정

```bash
# MySQL 데이터베이스 생성
mysql -u root -p
CREATE DATABASE store_db;
```

#### 4. Elasticsearch 시작 (Docker 사용)

```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.4
```

#### 5. 애플리케이션 실행

```bash
# Gradle Wrapper로 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew clean bootJar
java -jar build/libs/store-service-0.0.1-SNAPSHOT.jar
```

애플리케이션이 `http://localhost:8080`에서 시작됩니다.

#### 6. Elasticsearch 인덱스 초기화

```bash
# 인덱스 자동 생성 (애플리케이션 시작 시)
# 또는 수동으로 재인덱싱
curl -X POST http://localhost:8080/api/v1/admin/stores/reindex
```

---

## 📡 API 엔드포인트

### 매장 관리 (Owner API)

#### 내 매장 목록 조회
```http
GET /api/v1/owner/stores
Authorization: Bearer {JWT_TOKEN}
```

#### 매장 생성
```http
POST /api/v1/owner/stores
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "storeCategory": "CAFE",
  "name": "스타벅스 강남점",
  "phone": "02-1234-5678",
  "description": "신선한 원두로 만든 커피를 제공합니다",
  "businessNumber": "123-45-67890",
  "address": "서울시 강남구 테헤란로 123",
  "dong": "역삼동",
  "latitude": 37.5012,
  "longitude": 127.0396,
  "imageUrl": "https://example.com/image.jpg",
  "weekdayOpenTime": "09:00:00",
  "weekdayCloseTime": "22:00:00",
  "weekendOpenTime": "10:00:00",
  "weekendCloseTime": "23:00:00"
}
```

#### 매장 수정
```http
PUT /api/v1/owner/stores/{storeId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "스타벅스 강남점 (리뉴얼)",
  "description": "새롭게 단장한 카페입니다"
  // ... 기타 필드
}
```

#### 매장 삭제 (소프트 삭제)
```http
DELETE /api/v1/owner/stores/{storeId}
Authorization: Bearer {JWT_TOKEN}
```

#### 매장 상세 조회
```http
GET /api/v1/owner/stores/{storeId}
Authorization: Bearer {JWT_TOKEN}
```

### 고객용 API

#### 위치 기반 매장 검색
```http
GET /api/v1/stores?lat=37.5012&lng=127.0396&radius=5.0
```

**Parameters:**
- `lat`: 위도 (required)
- `lng`: 경도 (required)
- `radius`: 검색 반경 (km, default: 5.0)

#### 매장 상세 조회 (고객용)
```http
GET /api/v1/stores/{storeId}
```

### 검색 API

#### 하이브리드 검색 (BM25 + 벡터)
```http
GET /api/v1/stores/search?keyword=맛있는 커피
```
> 키워드 검색과 의미론적 검색을 결합하여 가장 관련성 높은 결과 제공

#### 시맨틱 검색 (벡터만)
```http
GET /api/v1/stores/search/semantic?keyword=조용하고 아늑한 분위기
```
> 의미적으로 유사한 매장 검색 (동의어, 유사 표현 포함)

#### 키워드 검색 (BM25만)
```http
GET /api/v1/stores/search/keyword?keyword=스타벅스
```
> 정확한 키워드 매칭에 특화된 검색

#### 자동완성
```http
GET /api/v1/stores/search/suggest?keyword=스타
```

### 내부 API (Internal Service Communication)

#### 매장 소유권 검증
```http
GET /internal/v1/stores/ownership?storeId={storeId}&memberId={memberId}
```

#### 소유자 매장 목록 (커서 기반)
```http
GET /internal/v1/stores/owner/{memberId}?lastStoreId={lastStoreId}&pageSize=10
```

#### 매장 정보 조회
```http
GET /internal/v1/stores/{storeId}
```

#### 여러 매장 조회
```http
POST /internal/v1/stores/query
Content-Type: application/json

[1, 2, 3, 4, 5]
```

#### 지역별 매장 ID 조회
```http
POST /internal/v1/stores/search
Content-Type: application/json

["역삼동", "삼성동", "청담동"]
```

### Admin API

#### 전체 데이터 재인덱싱
```http
POST /api/v1/admin/stores/reindex
Authorization: Bearer {ADMIN_TOKEN}
```

---

## 🔍 고급 검색 시스템

### 검색 전략 비교

| 검색 유형 | 장점 | 단점 | 사용 시나리오 |
|---------|-----|-----|------------|
| **하이브리드** | 정확도 + 의미 이해 | 약간 느림 | 일반적인 검색 |
| **시맨틱** | 동의어, 문맥 이해 | 정확한 키워드 약함 | "아늑한 분위기" 같은 추상적 검색 |
| **키워드** | 빠름, 정확한 매칭 | 오타 약함 | 브랜드명, 정확한 주소 검색 |

### 검색 예시

#### 예시 1: 하이브리드 검색
**검색어**: "맛있는 커피"

**BM25 결과** (키워드 매칭):
- "맛있는 커피집" (높은 점수)
- "커피 맛집" (중간 점수)

**벡터 검색 결과** (의미 유사도):
- "원두가 신선한 카페"
- "향긋한 에스프레소"
- "specialty coffee shop"

**최종 결과** (결합):
1. "맛있는 커피집" (키워드 + 벡터 모두 높음)
2. "원두가 신선한 카페" (벡터 점수 높음)
3. "커피 맛집" (키워드 점수 높음)

#### 예시 2: 시맨틱 검색
**검색어**: "데이트하기 좋은 곳"

**결과**:
- "로맨틱한 분위기의 레스토랑"
- "조용하고 아늑한 카페"
- "분위기 좋은 루프탑 바"

### OpenAI Embeddings 작동 원리

<img width="730" height="726" alt="image" src="https://github.com/user-attachments/assets/f49e5d19-3e11-43df-94c1-b8911355a3f9" />

### 검색 가중치 설정

```java
// StoreSearchService.java
.multiMatch(m -> m
    .fields("name^3.0",           // 매장명 가중치: 3.0
            "name.ngram^2.0",     // N-gram 가중치: 2.0
            "description^1.0",    // 설명 가중치: 1.0
            "address^1.5")        // 주소 가중치: 1.5
)

// 벡터 검색 가중치
.weight(5.0)  // 벡터 유사도 가중치: 5.0
```

---

## 💾 데이터베이스 구조

### ERD

- [Store ERD](https://github.com/CouponPop/.github/issues/4#issue-3636790940)

### Elasticsearch 인덱스 구조

```json
{
  "stores": {
    "mappings": {
      "properties": {
        "storeId": { "type": "long" },
        "memberId": { "type": "long" },
        "name": {
          "type": "text",
          "analyzer": "nori",
          "fields": {
            "keyword": { "type": "keyword" },
            "ngram": {
              "type": "text",
              "analyzer": "ngram_analyzer"
            }
          }
        },
        "description": {
          "type": "text",
          "analyzer": "nori"
        },
        "address": {
          "type": "text",
          "analyzer": "standard"
        },
        "dong": { "type": "keyword" },
        "storeCategory": { "type": "keyword" },
        "location": { "type": "geo_point" },
        "embedding": {
          "type": "dense_vector",
          "dims": 1536,
          "index": true,
          "similarity": "cosine"
        }
      }
    }
  }
}
```

---

## ⚙️ 환경 설정

### application.yml 프로파일

#### Local 환경 (`application-local.yml`)
```yaml
spring:
  datasource:
    master:
      hikari:
        jdbc-url: jdbc:mysql://localhost:3306/couponpop_store_local
    slave:
      hikari:
        jdbc-url: jdbc:mysql://localhost:3306/couponpop_store_local
```

#### Production 환경 (`application-prod.yml`)
```yaml
spring:
  config:
    import: aws-parameterstore:

  cloud:
    aws:
      parameter-store:
        enabled: true
        prefix: /couponpop/store-service
```

### Flyway 마이그레이션

마이그레이션 파일 위치: `src/main/resources/db/migration/`

```bash
# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 정보 확인
./gradlew flywayInfo

# 마이그레이션 검증
./gradlew flywayValidate
```

### Master-Slave 데이터베이스 설정

```java
// RoutingDataSource.java
@Transactional(readOnly = true)  // → Slave DB
public List<Store> findAll() { ... }

@Transactional  // → Master DB
public Store save(Store store) { ... }
```

---

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests StoreServiceTest

# 테스트 결과 확인
open build/reports/tests/test/index.html
```

### 코드 커버리지

```bash
# Jacoco 리포트 생성
./gradlew jacocoTestReport

# 커버리지 리포트 확인
open build/reports/jacoco/test/html/index.html
```

### Testcontainers 활용

```java
@Testcontainers
class StoreServiceTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb");
        
    @Test
    void createStore() {
        // 실제 MySQL 컨테이너로 통합 테스트
    }
}
```

---

## Health Check

```bash
# 서비스 상태 확인
curl http://localhost:8080/actuator/health

# 응답 예시
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "elasticsearch": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## 📊 모니터링

### Prometheus Metrics

메트릭 엔드포인트: `http://localhost:8080/actuator/prometheus`

**주요 메트릭**:
- `http_server_requests_seconds`: HTTP 요청 응답 시간
- `jvm_memory_used_bytes`: JVM 메모리 사용량
- `elasticsearch_search_duration`: Elasticsearch 검색 시간
- `openai_embedding_duration`: OpenAI API 호출 시간

### ELK Stack 로그 분석

#### Filebeat 설정
```yaml
# filebeat/config/filebeat.yml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /app/logs/*.log
    
output.logstash:
  hosts: ["logstash:5044"]
```

#### Logstash 파이프라인
```ruby
# logstash/pipeline/logstash.conf
input {
  beats {
    port => 5044
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level}" }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "store-service-logs-%{+YYYY.MM.dd}"
  }
}
```

---
<div align="center">

**<img width="20" height="20" alt="image" src="https://github.com/user-attachments/assets/dd2d9522-3591-454c-9cd2-0b534b75b8b9" /> CouponPop Team 9**

</div>
