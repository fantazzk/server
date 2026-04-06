import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-starter-data-jpa:4.0.3")
        classpath("org.jmolecules:jmolecules-ddd:2.0.1")
        classpath("org.jmolecules:jmolecules-layered-architecture:2.0.1")
        classpath("org.jmolecules.integrations:jmolecules-bytebuddy-nodep:0.33.0")
        classpath("org.jmolecules.integrations:jmolecules-spring:1.6.0")
        classpath("org.jmolecules.integrations:jmolecules-jackson:1.6.0")
        classpath("org.jmolecules.integrations:jmolecules-jpa:1.6.0")
        classpath("org.hibernate.orm:hibernate-core:7.2.4.Final")
    }
}

plugins {
    java
    `java-library`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    id("net.bytebuddy.byte-buddy-gradle-plugin") version "1.17.8"
}

group = findProperty("group") ?: group
version = findProperty("version") ?: version

repositories {
    mavenCentral()
}

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

dependencies {
    implementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))
    implementation(platform("org.springframework.modulith:spring-modulith-bom:2.0.5"))
    implementation(enforcedPlatform(libs.kotlin.bom))
    implementation(enforcedPlatform(libs.kotlinx.coroutine.bom))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-liquibase")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    implementation("org.springframework.modulith:spring-modulith-starter-insight")
    implementation("org.jmolecules:jmolecules-ddd:2.0.1")
    implementation("org.jmolecules:kmolecules-ddd:2.0.1")
    implementation("org.jmolecules.integrations:jmolecules-jpa:1.6.0")
    implementation("org.jmolecules:jmolecules-layered-architecture:2.0.1")
    implementation("org.jmolecules.integrations:jmolecules-spring:1.6.0")
    implementation("org.jmolecules.integrations:jmolecules-jackson:1.6.0")
    implementation("org.springframework:spring-tx")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
    implementation("io.sentry:sentry-spring-boot-4-starter:8.37.1")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation("org.springframework.boot:spring-boot-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

byteBuddy {
    transformation {
        plugin = org.jmolecules.bytebuddy.JMoleculesPlugin::class.java
    }
}

tasks.withType<net.bytebuddy.build.gradle.ByteBuddyTask>().configureEach {
    classPath.from(configurations.compileClasspath.get(), configurations.runtimeClasspath.get())
    discoverySet = configurations.runtimeClasspath.get()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}
