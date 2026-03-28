rootProject.name = "fantazzk-server"

include(":application-api")

include(":team-building:model")
include(":team-building:exception")
include(":team-building:infrastructure")
include(":team-building:service")
include(":team-building:repository-jdbc")
include(":team-building:api")
include(":team-building:schema")

pluginManagement {
    buildscript {
        repositories {
            gradlePluginPortal()
        }
    }

    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
