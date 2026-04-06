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
    id("net.bytebuddy.byte-buddy-gradle-plugin") version "1.17.8"
}

group = findProperty("group") ?: group
version = findProperty("version") ?: version

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))
    implementation(platform("org.springframework.modulith:spring-modulith-bom:2.0.5"))
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
    implementation("org.jmolecules.integrations:jmolecules-jpa:1.6.0")
    implementation("org.jmolecules:jmolecules-layered-architecture:2.0.1")
    implementation("org.jmolecules.integrations:jmolecules-spring:1.6.0")
    implementation("org.jmolecules.integrations:jmolecules-jackson:1.6.0")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
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
}
