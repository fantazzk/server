dependencies {
    implementation(project(":team-building:schema"))
    implementation(project(":template:api"))
    implementation(project(":template:repository-jdbc"))

    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-liquibase")

    runtimeOnly("org.postgresql:postgresql")

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
}
