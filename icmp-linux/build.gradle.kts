import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    alias(libs.plugins.cmake)
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.git.version) // https://stackoverflow.com/a/71212144
    alias(libs.plugins.sonatype.maven.central)
    alias(libs.plugins.gradleup.nmcp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":icmp-common"))
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.logback.classic)
}

cmake {
    sourceFolder=file("$projectDir/src/main")
}

tasks.withType<Test>().configureEach {
    // adds the cpp generated library to the classpath so the tests can find it
    systemProperty("java.library.path", project(":icmp-linux").layout.buildDirectory.asFile.get().absolutePath + "/cmake")
    dependsOn(":icmp-linux:cmakeBuild")

    testLogging {
        showStandardStreams = true
        outputs.upToDateWhen {true}
    }
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(":icmp-linux:cmakeBuild")
}

version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        branch(".+") { version = "\${ref}-SNAPSHOT" }
        tag("v(?<version>.*)") { version = "\${ref.version}" }
    }
}

// see: https://github.com/vanniktech/gradle-maven-publish-plugin/issues/747#issuecomment-2066762725
// and: https://github.com/GradleUp/nmcp
nmcp {
    val props = project.properties
    publishAllPublications {
        username = props["centralPortalToken"] as String? ?: ""
        password = props["centralPortalPassword"] as String? ?: ""
        // or if you want to publish automatically
        publicationType = "AUTOMATIC"
    }
}

// see: https://vanniktech.github.io/gradle-maven-publish-plugin/central/#configuring-the-pom
mavenPublishing {
    coordinates("com.jasonernst.icmp_lib", "icmp_linux", version.toString())
    pom {
        name = "ICMP Linux"
        description = "A library for sending and receiving ICMP packets on Linux"
        inceptionYear = "2024"
        url = "https://github.com/compscidr/icmp"
        licenses {
            license {
                name = "GPL-3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "compscidr"
                name = "Jason Ernst"
                url = "https://www.jasonernst.com"
            }
        }
        scm {
            url = "https://github.com/compscidr/icmp"
            connection = "scm:git:git://github.com/compscidr/icmp.git"
            developerConnection = "scm:git:ssh://git@github.com/compscidr/icmp.git"
        }
    }

    signAllPublications()
}