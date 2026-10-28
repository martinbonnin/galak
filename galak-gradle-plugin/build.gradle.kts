import com.gradleup.librarian.gradle.Librarian

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("com.google.devtools.ksp")
  id("com.gradleup.gratatouille.wiring")
}

Librarian.module(project)

gratatouille {
  codeGeneration {
    addDependencies.set(false)
  }
  pluginLocalPublication("net.mbonnin.galak")
}

dependencies {
  gratatouille(project(":galak-gradle-tasks"))
  compileOnly(libs.ksp.gradle)
  compileOnly(libs.kgp.min)
  compileOnly(libs.gradle.api.min)
  implementation(libs.gratatouille.wiring.runtime)
  testImplementation(libs.kotlin.test)
}
