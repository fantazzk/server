dependencies {
    implementation(project(":team-building:infrastructure"))
    implementation("org.liquibase:liquibase-core")

    integrationTestImplementation("org.springframework.boot:spring-boot-liquibase")
    integrationTestImplementation("org.testcontainers:testcontainers-mysql")
    integrationTestImplementation(project(":team-building:schema"))
    integrationTestRuntimeOnly("com.mysql:mysql-connector-j") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
}
