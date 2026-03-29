dependencies {
    implementation(project(":team-building:schema"))
    implementation(project(":template:api"))
    implementation(project(":template:repository-jdbc"))
    implementation(project(":room:api"))
    implementation(project(":room:infrastructure"))
    implementation(project(":room:repository-jdbc"))

    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-liquibase")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.testcontainers:testcontainers-postgresql")
}
