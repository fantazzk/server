rootProject.name = "fantazzk-server"

include(":application")
project(":application").projectDir = file("application-api")

include(":schema")

include(":room")
include(":room:model")
include(":room:exception")
include(":room:infrastructure")
include(":room:service")
include(":room:repository-jdbc")
include(":room:web")

include(":template")
include(":template:model")
include(":template:exception")
include(":template:infrastructure")
include(":template:api")
include(":template:service")
include(":template:repository-jdbc")
include(":template:web")

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
