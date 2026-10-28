plugins {
  alias(libs.plugins.kgp.jvm)
  alias(libs.plugins.ksp)
  alias(libs.plugins.galak)
}

galak {
  service("service") {
    packageName.set("com.example")
  }
}

dependencies {
  implementation(libs.galak.runtime)
  testImplementation(libs.kotlin.test)
}