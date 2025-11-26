plugins {
    id("java")
    application
}

group = "net.cubizor.cubiloc"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("net.cubizor.cubiloc.example.ExampleUsage")
}

repositories {
    mavenCentral()
    maven("https://repo.okaeri.cloud/releases")
    maven("https://nexus.cubizor.net/repository/maven-releases/") {
        credentials {
            username = project.findProperty("nexus.user") as String? ?: "admin"
            password = project.findProperty("nexus.password") as String? ?: "deneme"
        }
    }
}

dependencies {
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.13")
    implementation("eu.okaeri:okaeri-placeholders-core:5.1.2")
    implementation("net.kyori:adventure-api:4.25.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.25.0")
    implementation("net.kyori:adventure-text-minimessage:4.25.0")
    
    // Cubicolor - Color scheme support
    implementation("net.cubizor.cubicolor:cubicolor-api:1.4.0")
    implementation("net.cubizor.cubicolor:cubicolor-core:1.4.0")
    implementation("net.cubizor.cubicolor:cubicolor-exporter:1.4.0")
}
