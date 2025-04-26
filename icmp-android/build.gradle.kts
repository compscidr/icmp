plugins {
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.de.mannodermaus.android.junit5)
    alias(libs.plugins.android.library)
    alias(libs.plugins.git.version) // https://stackoverflow.com/a/71212144
    alias(libs.plugins.sonatype.maven.central)
    alias(libs.plugins.gradleup.nmcp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    id("signing") // https://medium.com/nerd-for-tech/oh-no-another-publishing-android-artifacts-to-maven-central-guide-9d7f300ebd74
    id("jacoco")
}

android {
    namespace = "com.jasonernst.icmp_lib"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("/META-INF/INDEX.LIST")
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/LICENSE-notice.md")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        configure<JacocoTaskExtension> {
            // Required for JaCoCo + Robolectric
            // https://github.com/robolectric/robolectric/issues/2230
            isIncludeNoLocationClasses = true

            // Required for JDK 11 with the above
            // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
            excludes = listOf("jdk.internal.*")
        }
        finalizedBy("jacocoTestReport")
    }
}

junitPlatform {
    // this is for the non-android unit tests, only required with the mannodermaus plugin
    jacocoOptions {
        html.enabled = true
        xml.enabled = true
        csv.enabled = false
    }
}

tasks.withType(JacocoReport::class.java) {
    executionData(fileTree("build/outputs/code_coverage/debugAndroidTest/connected/").include("**/*.ec"))
    executionData(fileTree("build/outputs/unit_test_code_coverage/debugUnitTest/").include("**/*.exec"))
}

kotlin {
    jvmToolchain(17)
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
    implementation(libs.logback.android)

    testImplementation(platform(libs.junit.bom))
    androidTestImplementation(libs.bundles.android.test)
    androidTestRuntimeOnly(libs.de.manodermaus.android.junit5.runner)
}

// see: https://github.com/vanniktech/gradle-maven-publish-plugin/issues/747#issuecomment-2066762725
// and: https://github.com/GradleUp/nmcp
nmcp {
    val props = project.properties
    centralPortal {
        username = props["centralPortalToken"] as String? ?: ""
        password = props["centralPortalPassword"] as String? ?: ""
        // or if you want to publish automatically
        publishingType = "AUTOMATIC"
    }
}

// see: https://vanniktech.github.io/gradle-maven-publish-plugin/central/#configuring-the-pom
mavenPublishing {
    coordinates("com.jasonernst.icmp", "icmp_android", version.toString())
    pom {
        name = "ICMP Android"
        description = "A library for sending and receiving ICMP packets on Android"
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