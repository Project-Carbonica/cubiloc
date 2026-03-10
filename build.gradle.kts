plugins {
    id("cubiloc.library-conventions")
}

version = project.findProperty("version") as String? ?: "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.okaeri.cloud/releases")
    maven("https://maven.pkg.github.com/Project-Carbonica/Cubicolor") {
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR") ?: ""
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN") ?: ""
        }
    }
}

dependencies {
    // Core
    implementation(libs.snakeyaml)
    implementation(libs.okaeri.placeholders)

    // Adventure
    implementation(libs.bundles.adventure)

    // Cubicolor
    implementation(libs.bundles.cubicolor)

    // Testing
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.assertj)
}

// Message key generation
tasks.register<GenerateMessageKeysTask>("generateMessageKeys") {
    yamlFile.set(file("src/main/resources/messages/en_US.yml"))
    outputDir.set(layout.buildDirectory.dir("generated/sources/messageKeys"))
    packageName.set("net.cubizor.cubiloc")
    objectName.set("M")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir(tasks.named("generateMessageKeys").map {
            layout.buildDirectory.dir("generated/sources/messageKeys")
        })
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateMessageKeys")
}
