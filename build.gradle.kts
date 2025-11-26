plugins {
    id("java")
    `java-library`
    `maven-publish`
}

group = "net.cubizor.cubiloc"
version = System.getenv("NYX_VERSION") ?: project.findProperty("version")?.toString() ?: "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.okaeri.cloud/releases")
    maven("https://nexus.cubizor.net/repository/maven-releases/") {
        credentials {
            username = project.findProperty("nexus.user") as String? ?: System.getenv("NEXUS_USERNAME") ?: ""
            password = project.findProperty("nexus.password") as String? ?: System.getenv("NEXUS_PASSWORD") ?: ""
        }
    }
}

dependencies {
    // Okaeri
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.13")
    implementation("eu.okaeri:okaeri-placeholders-core:5.1.2")
    
    // Kyori Adventure
    implementation("net.kyori:adventure-api:4.25.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.25.0")
    implementation("net.kyori:adventure-text-minimessage:4.25.0")
    
    // Cubicolor - Color scheme support
    implementation("net.cubizor.cubicolor:cubicolor-api:1.4.0")
    implementation("net.cubizor.cubicolor:cubicolor-core:1.4.0")
    implementation("net.cubizor.cubicolor:cubicolor-exporter:1.4.0")
    
    // Dependency Injection - Optional (compileOnly)
    compileOnly("com.google.inject:guice:7.0.0")
    compileOnly("com.google.dagger:dagger:2.57.2")
}


// Publishing configuration
apply(from = "publishing.gradle")

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}
