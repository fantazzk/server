dependencies {
    implementation(project(":team-building:schema"))
    implementation(project(":team-building:api"))
    implementation(project(":team-building:repository-jdbc"))

    testImplementation("org.testcontainers:testcontainers-mysql")
    runtimeOnly("com.mysql:mysql-connector-j") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
}
