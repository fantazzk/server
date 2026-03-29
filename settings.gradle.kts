rootProject.name = "fantazzk-server"

include(":application-api")

include(":team-building:schema")

include(":template:model")
include(":template:exception")
include(":template:infrastructure")
include(":template:service")
include(":template:repository-jdbc")
include(":template:api")
include(":template:application-api")

include(":room:model")
include(":room:exception")
include(":room:infrastructure")
include(":room:service")
include(":room:repository-jdbc")
include(":room:api")
include(":room:application-api")

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
