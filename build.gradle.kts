import com.gradleup.librarian.gradle.Librarian

plugins {
    id("base")
}
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.gratatouille)
        classpath(libs.ksp.gradle)
        classpath(libs.librarian.gradle.plugin)
        classpath(libs.wire.gradle.plugin)
        // workaround for https://github.com/Kotlin/kotlinx.serialization/issues/2803
        classpath(libs.kotlinx.serialization.core)
    }
}

Librarian.root(project)

tasks.register("docsNpmInstall", Exec::class.java) {
    enabled = file("docs").exists()

    commandLine("npm", "ci")
    workingDir("docs")
}

tasks.register("docsNpmBuild", Exec::class.java) {
    dependsOn("docsNpmInstall")

    enabled = file("docs").exists()

    commandLine("npm", "run", "build")
    workingDir("docs")
}

tasks.named("librarianStaticContent").configure {
    dependsOn("docsNpmBuild")

    val from = file("docs/dist")
    doLast {
        from.copyRecursively(outputs.files.single(), overwrite = true)
    }
}
