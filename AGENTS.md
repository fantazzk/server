# Fantazzk Server

## 프로젝트 형태

- 이 저장소는 단일 Gradle 프로젝트, 단일 Spring Boot 애플리케이션을 가진 Spring Modulith다.
- 루트 엔트리포인트는 `com.naminhyeok.fantazzk.FantazzkApplication` 하나만 둔다.
- 계획 문서와 스펙문서는 커밋하지 않는다
- 작업자들은 모두 한국인이기 때문에 커밋, PR, 테스트 함수의 이름 등은 한글로 작성한다.
- 작업들에 대하여 원자적 커밋으로 작업 내용을 남긴다.
- PR 본문은 최대한 꼼꼼히 작업사항에 대해서 작성한다. 제목에 [codex]를 추가하지 않는다
- 애플리케이션 모듈은 패키지 루트로 정의한다.
- 최상위 문서는 현재 모듈 목록 같은 스냅샷보다 오래 가는 규칙만 설명한다.
- 상세한 팀 공유 플레이북은 `.claude/skills/` 아래에 두고, 이 문서는 짧은 운영 규칙만 유지한다.

## 모듈 경계

- 모듈 루트 패키지의 public 타입만 다른 모듈에 공개되는 기본 계약으로 본다.
- 모듈 하위 패키지는 기본적으로 내부 구현으로 취급한다.
- 공개 surface는 최소로 유지한다.
- `web`, `config` 같은 같은-모듈 하위 패키지를 위해 모듈 루트 public 진입점을 둘 수 있다.
- 이런 public 진입점은 자동으로 cross-module API로 승격하지 말고, 다른 모듈이 의존하지 못하게 구조 테스트로 검증한다.
- aggregate와 핵심 도메인 개념은 모듈 루트 또는 `domain` 아래에 둘 수 있다.
- 모듈 규모가 커져 읽기 비용이 올라가면 `domain`, `application`, `query`, `repository`, `web`, `infrastructure` 같은 역할 패키지를 내부 패키지로 둘 수 있다.
- 한 모듈 안에서는 flat root 스타일과 layered internal package 스타일을 섞지 말고, 선택한 구조를 일관되게 유지한다.
- `application`, `api`, `repository`, `query`, `infrastructure`는 이름 자체가 목적이 아니라 읽기 비용과 책임 경계를 개선할 때만 둔다.
- `spi` 패키지는 기본 선택지로 두지 않는다.
- 의미 없는 port/adapter, interface/impl 쌍은 두지 않는다. 추상화는 모듈 경계, 도메인 개념, 기술적 제약을 분명히 드러낼 때만 유지한다.
- 새로 손대는 파일은 경로와 패키지 선언을 맞춘다. 기존 어긋남은 점진적으로 해소한다.

## 협력 규칙

- 같은 모듈 내부 협력은 direct call 을 기본으로 한다.
- layered internal package 스타일을 택한 모듈은 same-module direct call 을 위해 internal package 의 `public` 타입을 사용할 수 있다. 이런 타입은 cross-module contract 가 아니며, 다른 모듈 의존은 구조 테스트로 막는다.
- 모듈 간 필수 동기 협력은 explicit contract bean 으로 표현한다.
- 모듈 간 후속 반응이나 비동기 협력은 실제 consumer 가 있을 때만 domain event 로 표현한다.
- 다른 모듈의 repository, application service, query service를 직접 찌르지 않는다.
- 이벤트는 RPC 대체재처럼 쓰지 않는다. required consistency 는 direct contract 로 표현한다.
- application service의 기본 흐름은 `load -> invoke aggregate -> save` 다.
- application service는 same-module orchestration 을 위해 manual publisher 를 들고 다니지 않는다.
- listener 나 projection 은 가능한 한 event payload 만으로 처리하고, read-side 갱신만을 위해 write model 을 다시 조회하는 구조는 피한다.

## Java 와 Spring 규칙

- same-package 협력만으로 충분한 타입은 `package-private` 을 기본으로 한다.
- layered internal package 스타일에서는 same-module 협력을 위해 internal package 의 `public` 타입과 메서드를 허용한다.
- internal package 의 `public` 은 외부 모듈 공개 계약을 뜻하지 않는다. cross-module 접근 금지는 구조 테스트로 검증한다.
- 루트 애플리케이션은 `@SpringBootApplication`을 사용한다.
- 컴포넌트 스캔 기반 등록을 기본으로 한다.
- `@Configuration`은 실제 bean 조립이 필요한 경우에만 둔다.
- 단순히 이미 스캔 가능한 타입을 다시 export 하기 위한 wrapper configuration 은 만들지 않는다.
- 생성자 주입을 기본으로 한다.
- 도메인 개념은 가능하면 `jMolecules` 타입과 인터페이스로 먼저 표현하고, annotation 은 역할을 보강할 때만 사용한다.
- aggregate root 와 identifier 는 `org.jmolecules.ddd.types`를 우선 사용한다.
- application / query service 구현은 `org.jmolecules.ddd.annotation.Service`로 역할을 드러낸다.
- repository abstraction 은 `org.jmolecules.ddd.annotation.Repository`로 역할을 드러낸다.

## 테스트와 검증

- 테스트는 구현 과정을 기록하는 용도가 아니라 설계 규칙, 도메인 규칙, 외부 계약, 회귀 위험을 보호하는 실행 가능한 문서다.
- 테스트는 구현 세부보다 공개 계약과 관찰 가능한 결과를 우선 검증한다.
- domain 로직은 가능하면 Spring 없이 deterministic 한 unit test 로 검증할 수 있어야 한다.
- 시간, 난수, 사용자에게 의미 있는 코드 생성, 외부 상태 같은 비결정 요소는 경계 밖으로 밀어내고 주입 가능하게 만든다.
- 단순 기술적 UUID 생성까지 모두 래핑할 필요는 없다. 다만 규칙이 붙는 코드나 결과를 바꾸는 랜덤성은 명시적 generator 나 strategy 로 분리한다.
- 기본 test double 은 fake 와 stub 이다. mock 과 spy 는 제한적으로 사용한다.
- `@MockitoBean`은 `@WebMvcTest` 같은 Spring slice 나 모듈 경계 테스트에서 scope 제어가 필요할 때만 제한적으로 사용한다.
- fixture 는 builder 를 기본으로 하고, 테스트의 의미를 바꾸는 값은 테스트 본문에서 직접 드러낸다.
- fixture 가 반복되는 잡음을 줄이는 것은 좋지만, 핵심 전제를 숨기면 안 된다.
- 테스트 편의를 위해 production code 를 왜곡하지 않는다.
- 테스트 코드와 fixture 도 관리와 리팩토링의 대상이다. 구현 리팩토링에 과도하게 깨지는 테스트는 설계도 다시 본다.
- 유지할 테스트는 모듈 규칙, 도메인 invariant, 외부 계약, 실제 regression risk 를 보호해야 한다.
- thin delegation 이나 일회성 TDD scaffolding, 구현 choreography 만 보존하는 테스트는 정리 대상이다.
- 구조 테스트는 특정 클래스명 부재나 임의의 디렉터리 취향을 고정하기보다, 외부 모듈의 internal package 접근 금지, public contract 최소화, 실제 layer dependency 같은 성질을 우선 검증한다.
- 버그를 수정했다면 그 문제가 다시 발생하지 않도록 자동 검증을 추가하는 것을 기본 원칙으로 한다.
- regression test 는 내부 구현이 아니라 외부 계약, 도메인 규칙, 관찰 가능한 결과를 고정해야 한다.
- repository 테스트의 기본값은 `@DataJpaTest` 와 H2 다. 락, 격리 수준, DB vendor 고유 동작, 실제 migration 결과처럼 H2 로 충분히 증명할 수 없는 것은 `integrationTest` 에서 실제 데이터베이스로 검증한다.
- web contract 는 `@WebMvcTest` 로 검증한다.
- 모듈 구조는 `ApplicationModules.verify()` 로 항상 검증 가능해야 한다.
- 같은-모듈 하위 패키지를 위한 public root entrypoint 가 있다면, 다른 모듈이 그 타입에 의존하지 못하도록 structural rule 로 검증한다.
- 모듈 간 협력이 중요한 경우에만 `@ApplicationModuleTest`를 사용한다.
- 구현 중에는 대상 테스트나 `./gradlew test` 를 우선 사용한다.
- 로컬 작업을 마무리하기 전에는 `./gradlew check` 를 실행한다.
- commit 직전이나 review 전에는 `./gradlew integrationTest` 를 실행한다.
- `integrationTest` 는 unit test 만으로 충분히 증명할 수 없는 외부 경계와 실제 런타임 조합을 검증한다. 피드백 루프는 더 길지만 중요한 검증이므로 commit 전과 CI 에서는 반드시 포함한다.

## 에이전트가 피해야 할 것

- 현재 모듈 목록이나 임시 구조를 최상위 규칙으로 문서화하는 것
- 다른 모듈의 내부 패키지나 구현 타입에 직접 결합하는 것
- mock 을 기본값처럼 남발하는 것
- 구현 리팩토링을 막는 brittle test 를 늘리는 것
- 테스트 편의를 위해 production code 에 test-only hook 을 추가하는 것
- 이미 lower-cost test 가 보호하는 규칙을 다른 레이어에서 반복 검증하는 것
