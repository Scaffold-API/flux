plugins {
    base
    idea
}

repositories {
    mavenLocal()
    mavenCentral()
}

val fluxVersion = project.file("VERSION").readText().replace(System.lineSeparator(), "")
allprojects {
    group = "com.scaffold.api.flux"
    version = fluxVersion
}
println("Flux version: '${fluxVersion}'")

// Apply idea plugin to all integration tests.
subprojects {
    plugins.withId("java") {
        apply(plugin = "idea")
        afterEvaluate {
            val sourceSets = the<SourceSetContainer>()
            sourceSets.findByName("it")?.let {
                idea {
                    module {
                        testSources.from(sourceSets["it"].java.srcDirs)
                    }
                }
            }
        }
    }
}