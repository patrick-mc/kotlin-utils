plugins {
    `maven-publish`
    signing
    kotlin("jvm") version "1.3.71"
    java
    id("org.jetbrains.dokka") version "0.10.0"
}

group = "com.github.patrick-mc"
version = "0.3-beta"

repositories {
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://dl.bintray.com/kotlin/dokka")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("org.apache.commons:commons-lang3:3.10")
}

tasks {
    dokka {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/dokka"

        configuration {
            includeNonPublic = true
            jdkVersion = 8
        }
    }

    create<Jar>("dokkaJar") {
        archiveClassifier.set("javadoc")
        from(dokka)
        dependsOn(dokka)
    }

    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
}

try {
    publishing {
        publications {
            create<MavenPublication>("kotlinUtils") {
                from(components["java"])

                artifact(tasks["sourcesJar"])
                artifact(tasks["dokkaJar"])

                repositories {
                    mavenLocal()

                    maven {
                        name = "central"

                        credentials {
                            username = project.property("centralUsername").toString()
                            password = project.property("centralPassword").toString()
                        }

                        val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                    }
                }

                pom {
                    name.set("kotlin-utils")
                    description.set("Utils for my projects")
                    url.set("https://github.com/patrick-mc/kotlin-utils")

                    licenses {
                        license {
                            name.set("GNU General Public License v2.0")
                            url.set("https://opensource.org/licenses/gpl-2.0.php")
                        }
                    }

                    developers {
                        developer {
                            id.set("patrick-mc")
                            name.set("PatrickKR")
                            email.set("mailpatrickkorea@gmail.com")
                            url.set("https://github.com/patrick-mc")
                            roles.addAll("developer")
                            timezone.set("Asia/Seoul")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/patrick-mc/kotlin-utils.git")
                        developerConnection.set("scm:git:ssh://github.com:patrick-mc/kotlin-utils.git")
                        url.set("https://github.com/patrick-mc/kotlin-utils")
                    }
                }
            }
        }
    }

    signing {
        isRequired = true
        sign(tasks["jar"], tasks["sourcesJar"], tasks["dokkaJar"])
        sign(publishing.publications["kotlinUtils"])
    }
} catch (e: groovy.lang.MissingPropertyException) {}