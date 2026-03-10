plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
    `maven-publish`
}

group = "net.cubizor.cubiloc"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

// ============================================================
// Publishing
// ============================================================

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Cubiloc")
                description.set("Kotlin i18n library with Cubicolor semantic colors and Adventure components")
                url.set("https://github.com/Project-Carbonica/cubiloc")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("cubizor")
                        name.set("Cubizor Team")
                        email.set("dev@cubizor.net")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Project-Carbonica/cubiloc.git")
                    developerConnection.set("scm:git:ssh://github.com/Project-Carbonica/cubiloc.git")
                    url.set("https://github.com/Project-Carbonica/cubiloc")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Project-Carbonica/cubiloc")
            credentials {
                username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR") ?: ""
                password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
