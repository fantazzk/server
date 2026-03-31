dependencies {
    implementation(project(":template:infrastructure"))

    integrationTestImplementation(project(":schema"))
    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation("org.liquibase:liquibase-core")
    integrationTestImplementation("org.springframework.boot:spring-boot-liquibase")
    integrationTestRuntimeOnly("org.postgresql:postgresql")
}
