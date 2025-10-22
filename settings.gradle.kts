pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "flux"

// Smithy Build Plugins
include(":smithy-proofread")