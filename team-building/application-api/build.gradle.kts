dependencies {
    implementation(project(":team-building:schema"))
    implementation(project(":team-building:api"))
    implementation(project(":team-building:repository-jdbc"))

    runtimeOnly("org.postgresql:postgresql")

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
}
