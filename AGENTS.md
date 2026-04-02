# Fantazzk Server

## 기본 구조

- 이 저장소는 단일 Gradle 프로젝트, 단일 Spring Boot 애플리케이션을 가진 Spring Modulith다.
- 루트 엔트리포인트는 `com.naminhyeok.fantazzk.FantazzkApplication` 하나만 둔다.
- 핵심 애플리케이션 모듈은 `room`, `template`다.
- 각 모듈은 패키지로 경계를 나누며, 기본적으로 `CLOSED` 모듈로 취급한다.

## 패키지 규칙

각 모듈은 다음 역할 중심으로 정리한다.

- 모듈 루트 패키지
  aggregate root, value object, domain event, 식별자, 도메인 정책
- `application`
  유스케이스 오케스트레이션. load -> invoke aggregate -> save -> publish 역할만 담당
- `api`
  REST controller, exception handler, DTO
- `query`
  read model, projection writer/repository, query service
- `repository`
  persistence adapter, Spring Data JDBC wiring
- `spi`
  다른 모듈에 공개할 계약이 정말 필요할 때만 사용하는 named interface
- `infrastructure`
  외부 시스템 adapter나 모듈 내부 보조 wiring이 필요할 때만 사용

새로 손대는 파일은 경로와 패키지 선언을 맞춘다. 기존 어긋남은 점진적으로 해소한다.

## 모듈 경계

- `room`은 `template :: spi`만 참조할 수 있다.
- `template`는 `room` 내부 타입을 직접 참조하지 않는다.
- `api`와 `query`는 외부 모듈에 공개하는 surface가 아니다.
- named interface는 `template.spi`처럼 실제 cross-module contract가 있을 때만 만든다.
- read-side가 필요해도 그것만으로 named interface를 추가하지 않는다.

## 빈 등록 규칙

- 루트 애플리케이션은 `@SpringBootApplication`을 사용한다.
- 컴포넌트 스캔 기반 등록을 기본으로 한다. `@Service`, `@Component`, `@RestController` 사용 가능
- `@Configuration`은 실제 bean 조립이 필요한 경우에만 둔다.
  예: JDBC repository adapter 조립, SPI adapter 노출, 보안/인프라 설정
- 단순히 이미 스캔 가능한 타입을 `@Import`만 하는 wrapper configuration은 만들지 않는다.
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 기반 등록은 사용하지 않는다.

## 도메인 간 협력

- 기본은 application event 또는 named interface다.
- 다른 모듈의 repository, application service, query service를 직접 찌르지 않는다.
- aggregate는 자신의 이벤트를 만든다.
- application service는 저장 후 이벤트를 발행한다.
- projection/listener는 가능하면 event payload만으로 갱신되게 만들고, read-side 갱신을 위해 write repository를 다시 조회하는 구조는 피한다.

## CQRS / Read Model

- CQRS는 목표가 아니라 수단이다.
- `query` 패키지는 API 조회나 모듈 decoupling에 실질적 가치가 있을 때만 유지한다.
- write model을 그대로 다시 감싼 수준이면 projection을 늘리지 않는다.
- read-side는 모듈 내부 구현으로 유지하고, 외부 모듈 계약으로 승격하지 않는다.

## 테스트 기준

- `ApplicationModules.verify()`가 항상 통과해야 한다.
- module canvas / PlantUML 문서 생성 테스트를 유지한다.
- 모듈 간 협력은 `@ApplicationModuleTest`, `Scenario` 기반 테스트로 검증한다.
- aggregate 정책은 단위 테스트로 검증한다.
- 저장소/HTTP 동작은 통합 테스트로 검증한다.

## 문서화 기준

- 새 구조 판단 기준은 Oliver Drotbohm의 Spring Modulith 철학이다.
- 판단 우선순위는 다음과 같다.
  1. 모듈 경계가 분명한가
  2. 공개 surface가 최소인가
  3. 도메인 이벤트가 협력의 기본인가
  4. projection이 정말 필요한가
  5. 테스트가 모듈 경계를 증명하는가

## jMolecules 기준

- 도메인 개념은 가능하면 `jMolecules` 타입과 annotation으로 먼저 표현한다.
- aggregate root 와 identifier 는 `org.jmolecules.ddd.types`를 우선 사용한다.
- application / query service 구현은 `org.jmolecules.ddd.annotation.Service`로 역할을 드러낸다.
- repository abstraction 은 `org.jmolecules.ddd.annotation.Repository`로 역할을 드러낸다.
- `room.internal.*` 같은 우산 패키지는 강제하지 않는다. Modulith 기준에서 모듈 하위 패키지는 이미 internal 이므로 역할 중심 패키지(`application`, `api`, `query`, `repository`, `spi`)를 유지한다.
- JPA 전환 여부와 `jMolecules` 도입 여부는 분리해서 판단한다. `jMolecules` 도입이 곧 JPA 전환을 의미하지는 않는다.
