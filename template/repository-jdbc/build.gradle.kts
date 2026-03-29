dependencies {
    implementation(project(":template:infrastructure"))

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation(project(":team-building:schema"))
    integrationTestRuntimeOnly("org.postgresql:postgresql")
}
