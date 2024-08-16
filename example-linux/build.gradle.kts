plugins {
    id("java")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.git.version) // https://stackoverflow.com/a/71212144
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

version = "0.0.11"
gitVersioning.apply {
    refs {
        branch(".+") { version = "\${ref}-SNAPSHOT" }
        tag("v(?<version>.*)") { version = "\${ref.version}" }
    }
}

dependencies {
    implementation("com.jasonernst.icmp:icmp_linux:$version")
    implementation(libs.logback.classic)

    testImplementation(libs.bundles.unit.test)
}