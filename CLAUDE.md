# Fantazzk Server

## 모듈 구조

도메인 하나는 기본적으로 다음 모듈로 구성된다.

- `model` — 도메인 모델, 값 객체, 순수 도메인 규칙
- `exception` — 도메인 예외
- `schema` — Liquibase 마이그레이션
- `infrastructure` — repository port, 외부 도메인과 통신하기 위한 out-port 계약
- `service` — 유스케이스와 비즈니스 로직
- `repository-{type}` — `infrastructure`에 정의된 persistence port 구현
- `api` — REST controller, request/response DTO
- `application-{type}` — 실행 환경 설정과 모듈 조립
- `integration:X-Y` — 소비자 도메인 `X`의 out-port를 제공자 도메인 `Y`에 연결하는 어댑터

## 의존성 규칙

| 모듈 | 허용된 의존성 |
|------|-------------|
| model, exception, schema | 없음 |
| infrastructure | model |
| service | model, infrastructure, exception |
| repository-{type} | infrastructure |
| api | service, exception |
| integration:X-Y | X:infrastructure, Y:service |
| application-{type} | schema, api, repository-{type}, integration:* |

역방향 의존 금지.

상위 레이어가 하위 레이어를 침범하면 안 된다.

도메인 간 직접 의존은 허용하지 않는다.
도메인 간 연결은 반드시 소비자 소유 port + `integration:X-Y` 어댑터로 표현한다.

## 역할 원칙

### Model Module (`model`)

- 도메인의 핵심 개념과 규칙을 포함한다.
- 외부 기술, 저장소, 네트워크에 대한 관심사를 가지지 않는다.
- 가능한 한 순수하게 유지한다.
- Identity + Props → Model 인터페이스 패턴
- Props = 순수 프로퍼티만. 비즈니스 메서드는 확장 함수로 분리
- data class는 순수 값 객체. 검증(init)은 허용

### Service Module (`service`)

- 유스케이스와 비즈니스 흐름을 구현한다.
- 외부 시스템 접근은 직접 하지 않고 `infrastructure`의 port를 통해서만 수행한다.
- 다른 도메인의 내부 구현이나 타입에 직접 의존하지 않는다.
- public interface + `internal` impl
- 도메인 서비스가 개념 간 협업 조율. 서비스끼리 직접 호출 금지

### Infrastructure Module (`infrastructure`)

- persistence port와 external out-port의 계약만 정의한다.
- 구현을 포함하지 않는다.
- 소비자 도메인이 외부에 요구하는 최소 계약만 소유한다.

### Repository Module (`repository-{type}`)

- `infrastructure`에 정의된 persistence port를 구현한다.
- JDBC, JPA, Document DB 등 기술 세부사항은 이 계층에 머문다.
- Entity가 Model 인터페이스 직접 구현
- `@Column` bare annotation. enum 직접 매핑 (커스텀 컨버터)
- RepositoryImpl에서 `.toModel()`로 도메인 data class 반환. Entity 직접 반환 금지
- `@EnableJdbcRepositories`

### API Module (`api`)

- 외부 입력을 받는 driving adapter다.
- REST controller와 request/response DTO를 포함한다.
- 비즈니스 판단은 `service`에 위임한다.
- `@Import`로 컨트롤러 등록
- DTO는 `dto` 패키지, 파일별 분리

### Integration Module (`integration:X-Y`)

- 소비자 `X`의 out-port를 제공자 `Y`의 공개된 use case에 연결하는 adapter를 구현한다.
- 도메인 간 번역, 매핑, 예외 변환, 재시도, 캐시, 통신 기술 세부사항은 이 모듈에 위치한다.
- 현재 구현은 in-process 동기 호출일 수 있고, 이후 HTTP/gRPC/MQ/event/projection 기반으로 바뀔 수 있다.
- 통신 방식이 바뀌어도 소비자 도메인의 `service`와 `infrastructure` 계약은 유지되어야 한다.

### Application Module (`application-{type}`)

- 실행 환경 설정과 모듈 조립만 담당한다.
- 비즈니스 로직과 도메인 간 adapter 구현을 포함하지 않는다.
- 필요한 `integration` 모듈을 가져와 wiring만 수행한다.

## 도메인 간 통신

도메인 간 통신은 Consumer-Owned Port 패턴을 따른다.

- 소비자 도메인이 자신의 `infrastructure` 모듈에 out-port 인터페이스와 계약 타입을 소유한다.
- 제공자 도메인은 소비자 도메인의 존재를 몰라야 한다.
- 제공자 도메인 접근은 제공자 도메인의 공개된 `service` interface를 통해서만 이뤄진다.
- 도메인 간 adapter 구현은 항상 `integration:X-Y` 모듈에 위치한다.
- `application-*`는 integration 모듈을 조립만 하며, 통신 정책과 번역 로직을 직접 가지지 않는다.
- 소비자 도메인은 제공자 도메인의 entity, repository, controller, 내부 패키지를 직접 참조하면 안 된다.

## 이벤트와 통신 방식

이벤트, MQ, RPC, HTTP, in-process 호출은 모두 통신 구현 방식이다.
이들은 아키텍처 경계 자체가 아니라 `integration` 내부의 구현 선택이다.

- 즉시 응답과 강한 일관성이 필요하면 동기 호출을 사용한다.
- 느슨한 결합, fan-out, projection, eventual consistency가 필요하면 이벤트/MQ를 사용한다.
- 어떤 방식을 선택하더라도 도메인 경계는 port와 integration 모듈로 유지해야 한다.

## 빈 등록 규칙

- `@SpringBootApplication` 사용 금지
- 런처는 `@SpringBootConfiguration` + `@EnableAutoConfiguration`
- 모든 빈은 `@AutoConfiguration` + `@Bean`으로 명시적 등록
- `.imports` 파일에 반드시 등록
- `@ComponentScan`, `@Component`, `@Service`, `@Repository` 사용 금지

## 테스트

- AssertJ, 한글 테스트명
- 테스트 대상은 `cut` (Class Under Test) 명명
- repository 통합 테스트: `@DataJdbcTest` + `@ImportAutoConfiguration` + `@TestConstructor`
- application 통합 테스트: `@SpringBootTest` + `@AutoConfigureTestRestTemplate` + `@TestConstructor`
