import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

enum class ModuleKind {
    CONTAINER,
    MODEL,
    SERVICE,
    INFRASTRUCTURE,
    REPOSITORY_JDBC,
    API,
    APPLICATION_API,
    EXCEPTION,
    SCHEMA,
    INTEGRATION,
}

fun Project.moduleKind(): ModuleKind {
    val segments = path.trim(':').split(':').filter(String::isNotBlank)

    return when {
        segments.size == 1 && segments[0] == "application-api" -> ModuleKind.APPLICATION_API
        segments.size == 1 -> ModuleKind.CONTAINER
        segments.size == 2 && segments[0] == "integration" -> ModuleKind.INTEGRATION
        segments.size == 2 ->
            when (segments[1]) {
                "model" -> ModuleKind.MODEL
                "service" -> ModuleKind.SERVICE
                "infrastructure" -> ModuleKind.INFRASTRUCTURE
                "repository-jdbc" -> ModuleKind.REPOSITORY_JDBC
                "api" -> ModuleKind.API
                "application-api" -> ModuleKind.APPLICATION_API
                "exception" -> ModuleKind.EXCEPTION
                "schema" -> ModuleKind.SCHEMA
                else -> error("Unsupported project path: $path")
            }
        else -> error("Unsupported project path: $path")
    }
}

fun ModuleKind.isSpringModule(): Boolean =
    this == ModuleKind.SERVICE ||
        this == ModuleKind.API ||
        this == ModuleKind.REPOSITORY_JDBC ||
        this == ModuleKind.APPLICATION_API ||
        this == ModuleKind.INTEGRATION

fun ModuleKind.isWebModule(): Boolean =
    this == ModuleKind.API ||
        this == ModuleKind.APPLICATION_API

fun ModuleKind.isJdbcModule(): Boolean = this == ModuleKind.REPOSITORY_JDBC

fun ModuleKind.isApplicationModule(): Boolean = this == ModuleKind.APPLICATION_API

plugins {
    java
    `java-library`
    `jvm-test-suite`
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
}

allprojects {
    findProperty("group")?.let {
        group = it
    }
}

subprojects {
    val moduleKind = moduleKind()

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "jvm-test-suite")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    // 도메인별 모듈 이름 충돌 방지 (예: order:repository-jdbc → order-repository-jdbc)
    base {
        archivesName = if (parent != rootProject) "${parent?.name}-${name}" else name
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlin.RequiresOptIn",
                "-Xemit-jvm-type-annotations",
            )
        }
    }

    testing {
        suites {
            val test by getting(JvmTestSuite::class)
            val integrationTest by registering(JvmTestSuite::class)

            withType<JvmTestSuite> {
                useJUnitJupiter()

                targets {
                    all {
                        dependencies {
                            implementation(project())
                        }
                        testTask.configure {
                            shouldRunAfter(test)
                            testLogging {
                                events = mutableSetOf(TestLogEvent.FAILED)
                                exceptionFormat = TestExceptionFormat.FULL
                            }
                        }
                    }
                }
            }
        }
    }

    val integrationTestImplementation by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }

    tasks {
        val check by getting {
            dependsOn("integrationTest")
        }
    }

    dependencies {
        implementation(enforcedPlatform(rootProject.libs.kotlin.bom))
        implementation(enforcedPlatform(rootProject.libs.kotlinx.coroutine.bom))

        implementation(kotlin("reflect"))
        implementation(kotlin("stdlib"))

        testImplementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    }

    // Spring Boot modules
    if (moduleKind.isSpringModule()) {
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")

        dependencies {
            implementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
            implementation("org.springframework.boot:spring-boot-starter")
            implementation("tools.jackson.module:jackson-module-kotlin")
        }
    }

    // Web modules (REST API)
    if (moduleKind.isWebModule()) {
        dependencies {
            implementation("org.springframework.security:spring-security-core")
            implementation("org.springframework.boot:spring-boot-starter-web")
            implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
        }
    }

    // JDBC repository modules
    if (moduleKind.isJdbcModule()) {
        dependencies {
            api("org.springframework.boot:spring-boot-starter-data-jdbc")
            testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
        }
    }

    // Application modules (Spring Boot entry points)
    if (moduleKind.isApplicationModule()) {
        apply(plugin = "org.springframework.boot")

        dependencies {
            implementation("io.micrometer:micrometer-tracing-bridge-otel")
            implementation("org.springframework.boot:spring-boot-starter-actuator")
            implementation("org.springframework.boot:spring-boot-starter-web")

            testImplementation("org.springframework.boot:spring-boot-restclient")
            testImplementation("org.springframework.boot:spring-boot-resttestclient")
        }
    }
}
