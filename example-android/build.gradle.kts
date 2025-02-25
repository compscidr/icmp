plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.git.version) // https://stackoverflow.com/a/71212144
    alias(libs.plugins.compose.compiler)
}

version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        branch(".+") { version = "\${ref}-SNAPSHOT" }
        tag("v(?<version>.*)") { version = "\${ref.version}" }
    }
}

android {
    namespace = "com.jasonernst.icmp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jasonernst.icmp.icmp_android"
        minSdk = 29
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("/META-INF/INDEX.LIST")
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/LICENSE-notice.md")
        }
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.jasonernst.icmp:icmp_android:$version")
    implementation(libs.material) // required for the themes.xml
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.androidx.lifecycle)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.logback.classic)
    androidTestImplementation(libs.bundles.android.test)
    testRuntimeOnly(libs.bundles.test.runtime)
}