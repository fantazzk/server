dependencies {
    implementation(project(":template:infrastructure"))

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation("org.liquibase:liquibase-core")
    integrationTestImplementation("org.springframework.boot:spring-boot-liquibase")
    integrationTestImplementation(project(":team-building:schema"))
    integrationTestRuntimeOnly("org.postgresql:postgresql")
}
