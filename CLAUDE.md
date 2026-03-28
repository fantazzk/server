# Fantazzk Server

## 모듈 구조

도메인 하나는 8개 서브모듈로 구성된다: model, exception, schema, infrastructure, service, repository-jdbc, api, application-api.

## 의존성 규칙

| 모듈 | 허용된 의존성 |
|------|-------------|
| model, exception, schema | 없음 |
| infrastructure | model |
| service | model (api), infrastructure (impl), exception (impl) |
| repository-jdbc | infrastructure |
| api | service, exception |
| application-api | schema, api, repository-jdbc |

역방향 의존 금지. 도메인 간 직접 import 금지.

## 빈 등록

- `@SpringBootApplication`, `@ComponentScan`, `@Component`, `@Service`, `@Repository` 사용 금지
- 런처는 `@SpringBootConfiguration` + `@EnableAutoConfiguration`
- 모든 빈은 `@AutoConfiguration` + `@Bean`으로 명시적 등록
- `.imports` 파일에 반드시 등록

## Model

- Identity + Props → Model 인터페이스 패턴
- Props = 순수 프로퍼티만. 비즈니스 메서드는 확장 함수로 분리
- data class는 순수 값 객체. 검증(init)은 허용

## Service

- public interface + `internal` impl
- 도메인 서비스가 개념 간 협업 조율. 서비스끼리 직접 호출 금지

## Repository

- Entity가 Model 인터페이스 직접 구현
- `@Column` bare annotation. enum 직접 매핑 (커스텀 컨버터)
- RepositoryImpl에서 `.toModel()`로 도메인 data class 반환. Entity 직접 반환 금지
- `@EnableJdbcRepositories`

## API

- `@Import`로 컨트롤러 등록
- DTO는 `dto` 패키지, 파일별 분리

## 테스트

- AssertJ, 한글 테스트명
- 테스트 대상은 `cut` (Class Under Test) 명명
- repository 통합 테스트: `@DataJdbcTest` + `@ImportAutoConfiguration` + `@TestConstructor`
- application 통합 테스트: `@SpringBootTest` + `@AutoConfigureTestRestTemplate` + `@TestConstructor`

## 도메인 간 통신

Out-Port 패턴으로 도메인 간 의존을 역전한다.

- 소비자 도메인이 `infrastructure`에 Out-Port 인터페이스 + 계약 타입(DTO)을 소유
- 제공자 도메인은 소비자의 존재를 모름
- 어댑터(구현체)는 application-api 조립 시점에 주입
- Out-Port 인터페이스에는 소비자가 필요한 메서드만 정의 (제공자 API 전체가 아님)
