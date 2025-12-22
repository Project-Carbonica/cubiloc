plugins {
    id("java")
    `java-library`
    `maven-publish`
}

group = "net.cubizor.cubiloc"
version = project.findProperty("version") as String ?: "0.0.1-SNAPSHOT"

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

    // ByteBuddy for runtime enhancement (Lombok-style)
    implementation("net.bytebuddy:byte-buddy:1.14.11")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.test {
    useJUnitPlatform()
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

// ByteBuddy no longer needed - using direct field approach with Okaeri transformers
