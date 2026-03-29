dependencies {
    implementation(project(":team-building:schema"))
    implementation(project(":room:api"))
    implementation(project(":room:repository-jdbc"))
    implementation(project(":template:repository-jdbc"))
    implementation(project(":integration:room-template"))

    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-security")

    runtimeOnly("org.postgresql:postgresql")

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
}
