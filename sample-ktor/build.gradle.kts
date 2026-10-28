plugins {
    alias(libs.plugins.kgp.jvm)
    alias(libs.plugins.ksp)
    id("net.mbonnin.galak")
    id("application")
}

dependencies {
    implementation("net.mbonnin.galak:galak-ktor")
    // This sample uses netty as an engine.
    // See https://ktor.io/docs/server-dependencies.html#core-dependencies for more details
    implementation(libs.ktor.server.netty)
    implementation(libs.slf4j.simple)

    testImplementation(libs.kotlin.test)
}

galak {
    service("service") {
        packageName = "com.example"
    }
}

application {
    mainClass.set("com.example.MainKt")
}