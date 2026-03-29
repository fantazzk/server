dependencies {
    implementation(project(":team-building:schema"))
    implementation(project(":room:api"))
    implementation(project(":room:infrastructure"))
    implementation(project(":room:repository-jdbc"))
    implementation(project(":template:api"))

    runtimeOnly("org.postgresql:postgresql")

    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
}
