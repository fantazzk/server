# Fantazzk Server

## 기본 구조

- 이 저장소는 단일 Gradle 프로젝트, 단일 Spring Boot 애플리케이션을 가진 Spring Modulith다.
- 루트 엔트리포인트는 `com.naminhyeok.fantazzk.FantazzkApplication` 하나만 둔다.
- 핵심 애플리케이션 모듈은 `room`, `template`다.
- 각 모듈은 패키지로 경계를 나누며, 기본적으로 `CLOSED` 모듈로 취급한다.

## 패키지 규칙

- 애플리케이션 모듈은 모듈 루트 패키지로 정의한다.
- 모듈 루트 패키지의 public 타입만 다른 모듈에 공개되는 기본 계약으로 본다.
- 모듈 하위 패키지는 기본적으로 내부 구현으로 취급한다.
- aggregate와 핵심 도메인 개념은 모듈 루트 또는 `domain` 아래에 둘 수 있다. 한 모듈 안에서는 한 가지 스타일을 일관되게 유지한다.
- `application`은 유스케이스 오케스트레이션을 둔다. 기본 흐름은 `load -> invoke aggregate -> save`다.
- `api`, `repository`, `query`, `infrastructure`는 필요할 때만 둔다. Modulith가 요구하는 필수 구조는 아니다.
- DTO, exception, helper는 특정 패키지 배치를 강제하지 않는다. 다만 한 모듈 안에서는 일관성을 유지한다.
- cross-module contract는 모듈 루트 패키지에 최소 개수만 공개한다.
- `spi` 패키지는 사용하지 않는다.
- 의미 없는 port/adapter, interface/impl 쌍은 두지 않는다. 추상화는 모듈 경계, 도메인 개념, 기술적 제약을 분명히 드러낼 때만 유지한다.
- 새로 손대는 파일은 경로와 패키지 선언을 맞춘다. 기존 어긋남은 점진적으로 해소한다.

## 모듈 경계

- 다른 모듈은 모듈 루트에 공개된 계약 외 타입에 직접 의존하지 않는다.
- `room`은 `template` 모듈 루트에 공개된 계약만 참조할 수 있다.
- `template`는 `room` 내부 타입을 직접 참조하지 않는다.
- `api`, `application`, `repository`, `query` 같은 하위 패키지는 모듈 내부 구현 세부사항으로 본다.
- 모듈 간 필수 동기 협력은 명시적 contract bean 으로 표현한다.
- 모듈 간 후속 반응은 실제 consumer 가 있을 때만 event 로 표현한다.
- 이벤트는 RPC 대체재로 사용하지 않는다.
- named interface 는 기본적으로 도입하지 않는다.

## 빈 등록 규칙

- 루트 애플리케이션은 `@SpringBootApplication`을 사용한다.
- 컴포넌트 스캔 기반 등록을 기본으로 한다. `@Service`, `@Component`, `@RestController` 사용 가능
- `@Configuration`은 실제 bean 조립이 필요한 경우에만 둔다.
  예: JDBC repository adapter 조립, SPI adapter 노출, 보안/인프라 설정
- 단순히 이미 스캔 가능한 타입을 `@Import`만 하는 wrapper configuration은 만들지 않는다.
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 기반 등록은 사용하지 않는다.

## 도메인 간 협력

- 같은 모듈 내부 협력은 direct call 을 기본으로 한다.
- 모듈 간 필수 동기 협력은 explicit contract bean 을 기본으로 한다.
- 모듈 간 후속 반응이나 비동기 협력은 domain event 를 사용한다.
- 다른 모듈의 repository, application service, query service를 직접 찌르지 않는다.
- 이벤트는 RPC 대체재처럼 쓰지 않는다. required consistency 는 direct contract 로 표현한다.
- aggregate는 자신의 규칙과 상태 전이를 우선 책임진다. 이벤트는 실제 consumer 가 있을 때만 유지한다.
- application service는 manual publisher 를 들고 same-module orchestration 을 하지 않는다.
- listener/projection 이 필요하면 event payload 만으로 처리하고, read-side 갱신을 위해 write repository 를 다시 조회하는 구조는 피한다.

## CQRS / Read Model

- CQRS는 목표가 아니라 수단이다.
- `query` 패키지는 API 조회나 모듈 decoupling에 실질적 가치가 있을 때만 유지한다.
- write model을 그대로 다시 감싼 수준이면 projection을 늘리지 않는다.
- read-side는 모듈 내부 구현으로 유지하고, 외부 모듈 계약으로 승격하지 않는다.

## 테스트 기준

- 테스트는 구현 세부보다 설계 규칙과 모듈 경계를 우선 검증한다.
- 테스트는 특정 클래스 이름이나 패키지 배치를 고정하기보다, 공개 surface, 의존 방향, 협력 방식 같은 구조 규칙을 검증해야 한다.
- `ApplicationModules.verify()`가 항상 통과해야 한다.
- module canvas / PlantUML 문서 생성 테스트를 유지한다.
- 모듈 간 협력은 `@ApplicationModuleTest`로 검증한다.
- `PublishedEvents`, `Scenario`는 실제 module event 와 listener 가 있을 때만 사용한다.
- module event 가 없다면 direct state assertion 으로 검증한다.
- aggregate 정책은 단위 테스트로 검증한다.
- 저장소/HTTP 동작은 통합 테스트로 검증한다.

## 문서화 기준

- 새 구조 판단 기준은 Oliver Drotbohm의 Spring Modulith 철학이다.
- 판단 우선순위는 다음과 같다.
  1. 모듈 경계가 분명한가
  2. 공개 surface가 최소인가
  3. required consistency 와 decoupled reaction 이 정직하게 구분되는가
  4. projection이 정말 필요한가
  5. 테스트가 모듈 경계를 증명하는가

## jMolecules 기준

- 도메인 개념은 가능하면 `jMolecules` 타입과 annotation으로 먼저 표현한다.
- aggregate root 와 identifier 는 `org.jmolecules.ddd.types`를 우선 사용한다.
- application / query service 구현은 `org.jmolecules.ddd.annotation.Service`로 역할을 드러낸다.
- repository abstraction 은 `org.jmolecules.ddd.annotation.Repository`로 역할을 드러낸다.
- `room.internal.*` 같은 우산 패키지는 강제하지 않는다. Modulith 기준에서 모듈 하위 패키지는 이미 internal 이므로 역할 중심 패키지(`application`, `api`, `query`, `repository`, `spi`)를 유지한다.
- JPA 전환 여부와 `jMolecules` 도입 여부는 분리해서 판단한다. `jMolecules` 도입이 곧 JPA 전환을 의미하지는 않는다.
