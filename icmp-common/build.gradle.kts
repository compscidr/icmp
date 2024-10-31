plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.git.version) // https://stackoverflow.com/a/71212144
    alias(libs.plugins.sonatype.maven.central)
    alias(libs.plugins.gradleup.nmcp)
    id("jacoco")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


kotlin {
    jvmToolchain(17)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

dependencies {
    api(libs.packetdumper)
    api(libs.kotlinx.coroutines.core)
    api(libs.slf4j.api)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.logback.classic)
    testImplementation(libs.packetdumper)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
    coordinates("com.jasonernst.icmp", "icmp_common", version.toString())
    pom {
        name = "ICMP Common"
        description = "Common code for sending and receiving ICMP packets on Android and Linux"
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
            url = "https://github.com/compscidr/icmp-android"
            connection = "scm:git:git://github.com/compscidr/icmp-android.git"
            developerConnection = "scm:git:ssh://git@github.com/compscidr/icmp-android.git"
        }
    }

    signAllPublications()
}