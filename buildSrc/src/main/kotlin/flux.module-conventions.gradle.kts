plugins {
    id("flux.java-conventions")
    id("flux.maven-publishing-conventions")
}

val fluxVersion = project.file("${project.rootDir}/VERSION").readText().replace(System.lineSeparator(), "")

group = "com.scaffold-api.flux"
version = fluxVersion

/*
 * Licensing
 * ============================
 */
// Reusable license copySpec
val licenseSpec = copySpec {
    from("${project.rootDir}/LICENSE")
    from("${project.rootDir}/NOTICE")
}

/*
 * Extra Jars
 * ============================
 */
java {
    withJavadocJar()
    withSourcesJar()
}

// Suppress warnings in javadocs
tasks.withType<Javadoc>() {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:-html", "-quiet")
}

// Include an Automatic-Module-Name in all JARs.
afterEvaluate {
    val moduleName: String by extra
    tasks.withType<Jar> {
        metaInf.with(licenseSpec)
        inputs.property("moduleName", moduleName)
        manifest {
            attributes(mapOf("Automatic-Module-Name" to moduleName))
        }
    }
}

// Always run javadoc after build.
tasks["build"].dependsOn(tasks["javadoc"])
