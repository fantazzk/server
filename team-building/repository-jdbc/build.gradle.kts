dependencies {
    implementation(project(":team-building:infrastructure"))
    implementation("org.liquibase:liquibase-core")

    integrationTestImplementation("org.springframework.boot:spring-boot-liquibase")
    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation(project(":team-building:schema"))
    integrationTestRuntimeOnly("org.postgresql:postgresql")
}
