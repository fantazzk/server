dependencies {
    implementation(project(":room:infrastructure"))

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation(project(":team-building:schema"))
    integrationTestRuntimeOnly("org.postgresql:postgresql")
}
