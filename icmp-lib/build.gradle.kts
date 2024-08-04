plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.git.version) // https://stackoverflow.com/a/71212144
    alias(libs.plugins.sonatype.maven.central)
    id("signing") // https://medium.com/nerd-for-tech/oh-no-another-publishing-android-artifacts-to-maven-central-guide-9d7f300ebd74
}

android {
    namespace = "com.jasonernst.icmp_lib"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        branch(".+") { version = "\${ref}-SNAPSHOT" }
        tag("v(?<version>.*)") { version = "\${ref.version}" }
    }
}

// https://opensource.deepmedia.io/deployer
deployer {
    projectInfo {
        description = "A library for sending and receiving ICMP packets on Android"
        url = "https://github.com/compscidr/icmp-android/"
        groupId = "com.jasonernst"
        artifactId = "icmp-android"
        license("GPL-3.0", "https://www.gnu.org/licenses/gpl-3.0.en.html")
        developer("compscidr", "ernstjason1@gmail.com", "Jason Ernst", "https://www.jasonernst.com")
    }

    centralPortalSpec {
        // Take these credentials from the Generate User Token page at https://central.sonatype.com/account
        auth.user.set(secret("centralPortalToken"))
        auth.password.set(secret("centralPortalPassword"))

        // Signing is required
        signing.key.set(secret("signingKey"))
        signing.password.set(secret("signingKeyPassword"))
    }
}