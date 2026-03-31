import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

enum class ModuleKind {
    CONTAINER,
    MODEL,
    EXCEPTION,
    INFRASTRUCTURE,
    SERVICE,
    REPOSITORY_JDBC,
    WEB,
    API,
    APPLICATION,
    SCHEMA,
}

fun Project.moduleKind(): ModuleKind {
    val segments = path.trim(':').split(':').filter(String::isNotBlank)

    return when {
        segments.size == 1 && segments[0] == "application" -> ModuleKind.APPLICATION
        segments.size == 1 && segments[0] == "schema" -> ModuleKind.SCHEMA
        segments.size == 1 -> ModuleKind.CONTAINER
        segments.size == 2 ->
            when (segments[1]) {
                "model" -> ModuleKind.MODEL
                "exception" -> ModuleKind.EXCEPTION
                "infrastructure" -> ModuleKind.INFRASTRUCTURE
                "service" -> ModuleKind.SERVICE
                "repository-jdbc" -> ModuleKind.REPOSITORY_JDBC
                "web" -> ModuleKind.WEB
                "api" -> ModuleKind.API
                else -> error("Unsupported project path: $path")
            }
        else -> error("Unsupported project path: $path")
    }
}

fun ModuleKind.isSpringModule(): Boolean =
    this == ModuleKind.SERVICE ||
        this == ModuleKind.WEB ||
        this == ModuleKind.REPOSITORY_JDBC ||
        this == ModuleKind.APPLICATION

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
    val kind = moduleKind()

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "jvm-test-suite")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    if (kind.isSpringModule()) {
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }

    if (kind == ModuleKind.APPLICATION) {
        apply(plugin = "org.springframework.boot")
    }

    if (parent != rootProject) {
        group = "${rootProject.group}.${parent?.name}"
    }

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

    tasks.named("check") {
        dependsOn("integrationTest")
    }

    dependencies {
        implementation(enforcedPlatform(rootProject.libs.kotlin.bom))
        implementation(enforcedPlatform(rootProject.libs.kotlinx.coroutine.bom))
        implementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
        implementation(enforcedPlatform("org.springframework.modulith:spring-modulith-bom:2.0.5"))

        implementation(kotlin("reflect"))
        implementation(kotlin("stdlib"))
        implementation("org.springframework.modulith:spring-modulith-api")

        testImplementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
        testImplementation(enforcedPlatform("org.springframework.modulith:spring-modulith-bom:2.0.5"))
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
        testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
        testImplementation(rootProject.libs.mockk)
        testImplementation(rootProject.libs.springmockk)
    }

    if (kind.isSpringModule()) {
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter")
            implementation("org.springframework:spring-tx")
            implementation("tools.jackson.module:jackson-module-kotlin")
        }
    }

    if (kind == ModuleKind.WEB || kind == ModuleKind.APPLICATION) {
        dependencies {
            implementation("org.springframework.security:spring-security-core")
            implementation("org.springframework.boot:spring-boot-starter-web")
            implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
        }
    }

    if (kind == ModuleKind.REPOSITORY_JDBC || kind == ModuleKind.APPLICATION) {
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
            testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
        }
    }

    if (kind == ModuleKind.APPLICATION) {
        dependencies {
            implementation("org.springframework.modulith:spring-modulith-starter-core")
            implementation("io.sentry:sentry-spring-boot-4-starter:8.37.1")
            implementation("org.liquibase:liquibase-core")
            implementation("org.springframework.boot:spring-boot-liquibase")
            implementation("org.springframework.boot:spring-boot-starter-actuator")
            implementation("org.springframework.boot:spring-boot-starter-security")

            runtimeOnly("org.postgresql:postgresql")

            testImplementation("org.springframework.modulith:spring-modulith-starter-test")
            testImplementation("org.testcontainers:testcontainers-postgresql")
            testImplementation("org.springframework.boot:spring-boot-restclient")
            testImplementation("org.springframework.boot:spring-boot-resttestclient")
        }
    }
}
