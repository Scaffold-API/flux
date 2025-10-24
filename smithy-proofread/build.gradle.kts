plugins {
    id("flux.java-conventions")
    id("flux.maven-publishing-conventions")
    alias(libs.plugins.shadow)
}

description = "This module provides proofreading and spellchecking plugins for Smithy"

extra["displayName"] = "Scaffold :: Plugins :: Proofread"
extra["moduleName"] = "com.scaffold.api.plugins.language"

dependencies {
    api(libs.smithy.model)

    // TODO: build a shaded for french, spanish, german, polish etc.
    implementation(libs.langtool.en)

    // Suppress logging from the language tool
    implementation(libs.slf4j.nop)
}

tasks {
    // Shading is necessary for rule and dictionary
    // resource files to be correctly loaded by Smithy tooling.
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
    }

    jar {
        finalizedBy(shadowJar)
    }
}

configurePublishing {
    customComponent = components["shadow"] as SoftwareComponent
}
