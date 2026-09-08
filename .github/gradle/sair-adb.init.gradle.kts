// AGP 9.4's android-test-engine runs `adb` from a forked test worker. On the sair
// runners the scoped ADB port only exists in the CI step env, so pass it in
// explicitly (-PadbServerPort=...) and hand it to both ddmlib and the worker.
val port = gradle.startParameter.projectProperties["adbServerPort"]
if (!port.isNullOrBlank()) {
    System.setProperty("ANDROID_ADB_SERVER_PORT", port) // ddmlib reads this first
    allprojects {
        tasks.withType<Test>().configureEach {
            environment("ANDROID_ADB_SERVER_PORT", port)
            doFirst { println("SAIR-PROBE $path -P=$port taskEnv=${environment["ANDROID_ADB_SERVER_PORT"]} getenv=${System.getenv("ANDROID_ADB_SERVER_PORT")}") }
        }
    }
}
