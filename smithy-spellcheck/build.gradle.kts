plugins {
    id("flux.java-conventions")
}

description = "This module provides a spell checker plugin for Smithy"

extra["displayName"] = "Scaffold :: Plugins :: Spellcheck"
extra["moduleName"] = "com.scaffold.api.plugins.spellcheck"

dependencies {
    api(libs.smithy.model)
    implementation(libs.langtool.en)
}
