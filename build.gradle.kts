import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    `java-library`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
}

group = findProperty("group") ?: group
version = findProperty("version") ?: version

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

configure<KotlinJvmProjectExtension> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        freeCompilerArgs =
            listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlin.RequiresOptIn",
                "-Xemit-jvm-type-annotations",
            )
    }
}

val modulithVersion = "2.0.5"
val integrationTestSourceSet =
    sourceSets.create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
}

val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get(), configurations.testRuntimeOnly.get())
}

dependencies {
    implementation(platform("org.springframework.modulith:spring-modulith-bom:$modulithVersion"))
    testImplementation(platform("org.springframework.modulith:spring-modulith-bom:$modulithVersion"))
    integrationTestImplementation(platform("org.springframework.modulith:spring-modulith-bom:$modulithVersion"))

    implementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    implementation(enforcedPlatform(libs.kotlin.bom))
    implementation(enforcedPlatform(libs.kotlinx.coroutine.bom))

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-tx")
    implementation("org.jmolecules:jmolecules-ddd:1.9.0")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    implementation("org.springframework.modulith:spring-modulith-starter-insight")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
    implementation("io.sentry:sentry-spring-boot-4-starter:8.37.1")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-liquibase")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)

    integrationTestImplementation(sourceSets.main.get().output)
    integrationTestImplementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.springframework.boot:spring-boot-data-jpa-test")
    integrationTestImplementation("org.springframework.boot:spring-boot-jdbc-test")
    integrationTestImplementation("org.springframework.modulith:spring-modulith-starter-test")
    integrationTestImplementation("org.springframework.boot:spring-boot-restclient")
    integrationTestImplementation("org.springframework.boot:spring-boot-resttestclient")
    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestRuntimeOnly("org.postgresql:postgresql")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

val integrationTest =
    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        shouldRunAfter(tasks.test)
        useJUnitPlatform()
        testLogging {
            events = mutableSetOf(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

tasks.check {
    dependsOn(integrationTest)
}

tasks.named<Copy>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
