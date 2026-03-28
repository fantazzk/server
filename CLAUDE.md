# Fantazzk Server

## 모듈 구조

도메인 하나는 8개 서브모듈로 구성된다:

- `model` — 도메인 모델. 외부 의존 없음
- `exception` — 도메인 예외. 외부 의존 없음
- `schema` — Liquibase 마이그레이션. 외부 의존 없음
- `infrastructure` — 포트 인터페이스 (repository, 외부 도메인 포트). model만 의존
- `service` — 도메인 서비스, AutoConfiguration. model, infrastructure, exception 의존
- `repository-jdbc` — infrastructure 포트의 JDBC 구현. infrastructure 의존
- `api` — REST 컨트롤러, DTO. service, exception 의존
- `application-api` — 독립 실행 가능한 Spring Boot 런처. schema, api, repository-jdbc 의존

## 의존성 규칙

| 모듈 | 허용된 의존성 |
|------|-------------|
| model | 없음 |
| exception | 없음 |
| schema | 없음 |
| infrastructure | model |
| service | model (api), infrastructure (implementation), exception (implementation) |
| repository-jdbc | infrastructure |
| api | service, exception |
| application-api | schema, api, repository-jdbc |
| integration:X-Y | X의 infrastructure + Y의 service |

### 금지 규칙

- 역방향 의존 금지: 하위 레이어가 상위 레이어를 의존할 수 없음
- api → repository-jdbc 직접 참조 금지 (반드시 service 경유)
- 도메인 간 직접 import 금지

## 빈 등록 규칙

`@SpringBootApplication` 사용 금지. ComponentScan 없이 모듈 경계를 Gradle 서브모듈로 물리적으로 강제하기 위함이다. 도메인 간 직접 참조가 컴파일 타임에 차단되므로 변경 영향 범위가 자동으로 제한된다.

- 런처는 `@SpringBootConfiguration` + `@EnableAutoConfiguration` 사용
- 모든 빈은 `@AutoConfiguration` 클래스에서 `@Bean`으로 명시적 등록
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 반드시 등록
- 컴포넌트 스캔 어노테이션 사용 금지: `@ComponentScan`, `@Component`, `@Service`, `@Repository`

## Model 레이어 규칙

### Identity + Props → Model 인터페이스 패턴

모든 도메인 개념은 세 가지 인터페이스로 구성된다:

```kotlin
// Identity — 식별자
interface XxxIdentity {
    companion object
    val xxxId: Long
}
internal data class SimpleXxxIdentity(override val xxxId: Long) : XxxIdentity
fun XxxIdentity.Companion.of(xxxId: Long): XxxIdentity = SimpleXxxIdentity(xxxId)

// Props — 순수 프로퍼티만 (비즈니스 메서드 금지)
interface XxxProps {
    val name: String
    // fun isActive() — 금지. 확장 함수로 분리
}

// Model — Identity + Props 합성
interface XxxModel : XxxIdentity, XxxProps
```

### data class — 순수 값 객체

```kotlin
data class Xxx(...) : XxxModel
```

- 비즈니스 로직 최소화. 검증(init 블록)은 허용
- 비즈니스 메서드는 Model 인터페이스의 확장 함수로 분리

### Props 인터페이스에 금지되는 것

- `fun` 선언 금지 (확장 함수로 분리)
- 계산된 프로퍼티(`val picksPerTeam get() = ...`) 금지 (확장 함수로 분리)

## Service 레이어 규칙

### public interface + internal impl

```kotlin
interface XxxService {
    fun doSomething(): Result
}

internal class XxxServiceImpl(
    private val repository: XxxRepository,
) : XxxService {
    override fun doSomething(): Result { ... }
}
```

- `internal` 키워드로 구현 클래스 캡슐화
- AutoConfiguration에서 `@Bean`으로 등록, 반환 타입은 인터페이스

### 도메인 서비스

여러 도메인 개념의 협업이 필요한 로직은 도메인 서비스가 조율한다.
서비스끼리 직접 호출하지 않는다. 도메인 간 통신은 포트를 통해서만.

## Repository 레이어 규칙

### Entity — Model 인터페이스 직접 구현

```kotlin
@Table("xxx")
class XxxEntity(
    @Column override val name: String,
    @Column override val status: XxxStatus,  // enum 직접 매핑
) : XxxModel {
    @Id
    var id: Long = 0L
    override val xxxId: Long get() = id
}
```

- `@Column` — bare annotation (explicit column name 금지, 네이밍 전략에 위임)
- `@Id var id` — 클래스 바디에 선언
- enum 타입 직접 매핑 (String 변환 금지, 커스텀 컨버터로 처리)

### CrudRepository + RepositoryImpl

```kotlin
interface XxxJdbcCrudRepository : CrudRepository<XxxEntity, Long>

class XxxRepositoryImpl(
    private val jdbcRepository: XxxJdbcCrudRepository,
) : XxxRepository {
    override fun findById(id: XxxIdentity): XxxModel? =
        jdbcRepository.findById(id.xxxId).orElse(null)?.toModel()

    private fun XxxEntity.toModel() = Xxx(xxxId = id, name = name, ...)
}
```

- `RepositoryImpl`은 Entity ↔ 도메인 data class 변환 어댑터
- `.toModel()`로 반드시 도메인 data class 반환 (Entity 직접 반환 금지)
- AutoConfiguration에 `@EnableJdbcRepositories` 선언

## API 레이어 규칙

- `@Import`로 컨트롤러 등록 (`@Bean` 아님)
- DTO는 `dto` 패키지에 파일별 분리
- ExceptionHandler는 `@RestControllerAdvice`

## 테스트 규칙

### 단위 테스트 (model, service)

- AssertJ 사용 (`assertThat`, `assertThatThrownBy`)
- 테스트명 한글
- service 테스트는 InMemory fake repository 사용

### 통합 테스트 (repository-jdbc)

```kotlin
@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
    XxxJdbcConfiguration::class,
    XxxRepositoryAutoConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class XxxRepositoryIntegrationTest(
    private val cut: XxxRepository,         // cut = Class Under Test
    private val jdbcRepository: XxxJdbcCrudRepository,  // 데이터 세팅용
)
```

### 통합 테스트 (application-api)

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IntegrationTest(private val restTemplate: TestRestTemplate)
```

## 도메인 간 통신

각 도메인 모듈은 독립된 바운디드 컨텍스트다. 도메인 간 통신은 정말 필요할 때만 도입한다.

필요한 경우 Consumer-Owned Port 패턴을 따른다:

- 소비자 도메인이 포트 인터페이스와 계약 타입을 자신의 `infrastructure` 모듈에 소유
- 제공자 도메인은 소비자 도메인의 존재를 모름
- 어댑터 구현은 `integration:X-Y` 모듈에 위치
- 루트 `application-api`는 통합 로직 없이 조립만 담당

## 새 도메인 추가 절차

1. `settings.gradle.kts`에 8개 서브모듈 등록 (컨테이너 모듈은 include하지 않음)
2. 각 서브모듈의 `build.gradle.kts` 생성 (의존성 규칙 준수)
3. model: Identity + Props → Model 인터페이스 + data class
4. exception: RuntimeException 서브클래스
5. infrastructure: Repository 포트 인터페이스
6. service: interface + internal impl + AutoConfiguration + .imports
7. schema: Liquibase YAML + SQL
8. repository-jdbc: Entity + CrudRepository + RepositoryImpl + AutoConfiguration + .imports
9. api: Controller + DTO + ExceptionHandler + AutoConfiguration + .imports
10. application-api: 독립 런처 + 통합 테스트
