plugins {
    alias(libs.plugins.kgp.jvm)
    alias(libs.plugins.ksp)
    id("net.mbonnin.galak")
    id("application")
}

dependencies {
    implementation("net.mbonnin.galak:galak-http4k")
    implementation(platform(libs.http4k.bom.get()))
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.netty)
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