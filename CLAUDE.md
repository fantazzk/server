# Fantazzk Server

## 모듈 구조

도메인 하나는 8개 서브모듈로 구성된다:

- `model` — 도메인 모델, 값 객체. 외부 의존 없음
- `exception` — 도메인 예외. 외부 의존 없음
- `schema` — Liquibase 마이그레이션. 외부 의존 없음
- `infrastructure` — 포트 인터페이스 (repository, 외부 도메인 포트). model만 의존
- `service` — 비즈니스 로직, AutoConfiguration. infrastructure와 exception 의존
- `repository-jdbc` — infrastructure 포트의 JDBC 구현. infrastructure 의존
- `api` — REST 컨트롤러, DTO. service와 exception 의존
- `application-api` — 독립 실행 가능한 Spring Boot 런처. schema, api, repository-jdbc 의존

## 의존성 방향

```
model, exception, schema → 외부 의존 없음
infrastructure → model
service → infrastructure, exception
repository-jdbc → infrastructure
api → service, exception
application-api → schema, api, repository-jdbc
integration:X-Y → X의 infrastructure + Y의 service
```

역방향 의존 금지. 상위 레이어가 하위 레이어를 의존해서는 안 된다.

## 빈 등록 규칙

- `@SpringBootApplication` 사용 금지
- 런처는 `@SpringBootConfiguration` + `@EnableAutoConfiguration` 사용
- 모든 빈은 `@AutoConfiguration` 클래스에서 `@Bean`으로 명시적 등록
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 반드시 등록
- 컴포넌트 스캔 어노테이션 사용 금지: `@ComponentScan`, `@Component`, `@Service`, `@Repository`

## 도메인 간 통신

각 도메인 모듈은 독립된 바운디드 컨텍스트다. 도메인 간 통신은 정말 필요할 때만 도입한다.

필요한 경우 Consumer-Owned Port 패턴을 따른다:

- 소비자 도메인이 포트 인터페이스와 계약 타입을 자신의 `infrastructure` 모듈에 소유
- 제공자 도메인은 소비자 도메인의 존재를 모름
- 어댑터 구현은 `integration:X-Y` 모듈에 위치
- 루트 `application-api`는 통합 로직 없이 조립만 담당
