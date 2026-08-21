plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "de.thegame4craft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.minestom:minestom:2026.08.16-26.2")
    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("org.slf4j:slf4j-simple:2.0.18")
    implementation("io.hotmoka:toml4j:0.7.3")
    implementation("de.articdive:jnoise-pipeline:4.1.0")

}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25)) // Minestom has a minimum Java version of 21
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "de.thegame4craft.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}

tasks.test {
    useJUnitPlatform()
}