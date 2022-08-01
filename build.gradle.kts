plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.7.0"
    id ("io.freefair.lombok") version "6.5.0.3"
    jacoco
}

val junit5Version = "5.3.1"

group = "com.valoler"
version = "1.0-SNAPSHOT"

dependencies{
    // https://mvnrepository.com/artifact/commons-validator/commons-validator
    implementation("commons-validator:commons-validator:1.7")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.11")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
}

// This might not be needed in the future, but as of present the default version bundled with the latest version of gradle does not work with Java 11
jacoco {
    toolVersion = "0.8.2"
}

repositories {
    mavenCentral()
    maven (
        "https://www.jetbrains.com/intellij-repository/releases"
    )
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2019.3.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("Git4Idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("193")
        untilBuild.set("222.*")
    }

    instrumentCode{
        compilerVersion.set("222.3345.118")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    "test"(Test::class) {
        useJUnitPlatform()
    }

    val codeCoverageReport by creating(JacocoReport::class) {
        executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

        subprojects.onEach {
            sourceSets(it.sourceSets["main"])
        }

        reports {
            sourceDirectories.setFrom(files(sourceSets["main"].allSource.srcDirs))
            classDirectories.setFrom(files(sourceSets["main"].output))
            xml.required.set(true)
            xml.outputLocation.set(File("$buildDir/reports/jacoco/report.xml"))
            html.required.set(false)
            csv.required.set(false)
        }

        dependsOn("test")
    }

//    // Generate code coverage reports ... run with jacoco
//    jacocoTestReport {
//        reports {
//            csv.required.set(false)
//            xml.required.set(true)
//            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
//        }
//        dependsOn("test")
//    }
}
