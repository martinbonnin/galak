pluginManagement {
  listOf(repositories, dependencyResolutionManagement.repositories).forEach {
    it.mavenCentral()
    it.maven("https://storage.googleapis.com/gradleup/m2")
    it.maven("https://storage.googleapis.com/apollo-previews/m2/")
  }
}

include(
  "galak-processor",
  "galak-gradle-plugin",
  "galak-gradle-tasks",
  "galak-runtime",
  "galak-ktor",
  "galak-http4k",
  "galak-spring",
  "galak-subgraph",
  "galak-apollo-reporting"
)

