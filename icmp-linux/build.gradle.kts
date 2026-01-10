import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    alias(libs.plugins.cmake)
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
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = false
    }
}

version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        branch(".+") { version = "\${ref}-SNAPSHOT" }
        tag("v(?<version>.*)") { version = "\${ref.version}" }
    }
}

dependencies {
    api("com.jasonernst.icmp:icmp_common:$version")
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.logback.classic)
    testRuntimeOnly(libs.bundles.test.runtime)
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
    dependsOn(":icmp-linux:cmakeBuild")
    dependsOn(":icmp-linux:jar")
    finalizedBy("jacocoTestReport")
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(":icmp-linux:cmakeBuild")
}

// see: https://github.com/vanniktech/gradle-maven-publish-plugin/issues/747#issuecomment-2066762725
// and: https://github.com/GradleUp/nmcp
nmcpAggregation {
    val props = project.properties
    allowEmptyAggregation = true
    centralPortal {
        username = props["centralPortalToken"] as String? ?: ""
        password = props["centralPortalPassword"] as String? ?: ""
        // or if you want to publish automatically
        publishingType = "AUTOMATIC"
    }
}

// see: https://vanniktech.github.io/gradle-maven-publish-plugin/central/#configuring-the-pom
mavenPublishing {
    coordinates("com.jasonernst.icmp", "icmp_linux", version.toString())
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

afterEvaluate {
    getTasksByName("signSoPublication", true).forEach {
        it.dependsOn(":icmp-linux:cmakeBuild")
    }
}

tasks.jar {
    from(project(":icmp-linux").layout.buildDirectory.asFile.get().absolutePath + "/cmake/libicmp.so")
}

val so: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, JavaVersion.current().majorVersion.toInt())
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("so"))
    }
}

artifacts {
    add("so", file(project(":icmp-linux").layout.buildDirectory.asFile.get().absolutePath + "/cmake/libicmp.so"))
}