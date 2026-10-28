---
title: "Getting started"
---

Galak is an [implementation-first](https://jordaneldredge.com/implementation-first/) GraphQL execution library.

Galak:

* Generates a GraphQL schema from your Kotlin code: write Kotlin, get a typesafe API.
* Doesn't use reflection. Use it on the JVM and enjoy ultra-fast start times. Or use it with Kotlin native. Galak is KMP-ready!
* Supports custom scalars, subscriptions, persisted queries and everything in the current [GraphQL draft](https://spec.graphql.org/draft/).
* Integrates with [Ktor](/galak/ktor/), [http4k](/galak/http4k/) and [Spring](/galak/spring/).
* Supports [Apollo Federation](/galak/federation/).

Under the hood, Galak uses [KSP](https://kotlinlang.org/docs/ksp-overview.html) to generate GraphQL resolvers and types from your Kotlin code.

## Gradle configuration

Galak comes with a Gradle plugin that:

* Configures KSP:
  * Adds `galak-processor` to the KSP configuration.
  * Configure `service` & `packageName` KSP arguments.
* Configures dependency resolution to align Galak versions if none is specified.
* Adds `galakCheckSchema` and `galakDumpSchema` tasks (see [Monitoring the generated schema](/galak/schema-dump/))

Configure your Gradle build:

```kotlin
// build.gradle.kts
plugins {
  // Kotlin and KSP are required
  id("org.jetbrains.kotlin.jvm").version(kotlinVersion)
  id("com.google.devtools.ksp").version(kspVersion)
  // Add the Galak plugin
  id("net.mbonnin.galak").version("%latest_version%")
}

dependencies {
  // Add the runtime dependency
  implementation("net.mbonnin.galak:galak-runtime")
}

// Configure codegen
galak {
  service("service") {
    packageName = "com.example"
  }
}
```

:::note
Galak requires Java 17
:::

Disable KSP for tests ([doc](https://kotlinlang.org/docs/ksp-multiplatform.html#avoid-the-ksp-configuration-on-ksp-1-0-1)):

```
ksp.allow.all.target.configuration=false
```

## Define your root query

Write your root query class in a `Query.kt` file:

```kotlin
// @GraphQLQuery is the entry point for KSP processing
@GraphQLQuery
class Query {
  // Public functions become GraphQL fields 
  // Kdoc comments become GraphQL descriptions
  // Kotlin parameters become GraphQL arguments
  /**
   * Greeting for name
   */
  fun hello(name: String): String {
    return "Hello $name"
  }
}
```

Run the codegen:

```shell
./gradlew kspKotlin
# or if multiplatform
./gradlew kspCommonMainKotlinMetadata
```

## Execute your query

The codegen generates a `com.example.ServiceExecutableSchemaBuilder` class that is the entry point to execute GraphQL requests:

```kotlin
val executableSchema = ServiceExecutableSchemaBuilder()
  .build()
```

Create a GraphQL request:
```kotlin
val request = GraphQLRequest.Builder()
  .document("{ hello(name: \"sample\") }")
  .build()
```

Execute the GraphQL request:
```kotlin
val response = executableSchema.execute(
  request,
  ExecutionContext.Empty
)

println(response.data)
// {hello=Hello sample}
```

Galak supports objects, interfaces, unions, enum, input objects, deprecation, customizing names and descriptions and more. 

See the [Generating a schema](/galak/schema/) page for more details.

## Integrate with your favorite server library

Galak comes with integrations for popular server libraries:

* [Ktor integration](/galak/ktor/)
* [http4k integration](/galak/http4k/)
* [Spring integration](/galak/spring/)

See the respective documentation for how to configure a server with an `ExecutableSchema`.
