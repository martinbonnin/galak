---
title: "Ktor"
---

> See [sample-ktor](https://github.com/martinbonnin/galak/tree/main/sample-ktor) for a project using the Ktor integration. 

## Add galak-ktor to your project

> Make sure to follow the steps in ["Getting started"](/galak/) first to generate your `ServiceExecutableSchemaBuilder`.

To use the Ktor integration, add `galak-ktor` to your dependencies and a Ktor engine:

```kotlin
dependencies {
  // Add the runtime dependency
  implementation("net.mbonnin.galak:galak-ktor:%latest_version%")
  // This sample uses netty as an engine.
  // See https://ktor.io/ for other choices.
  implementation("io.ktor:ktor-server-netty:3.0.0")
}
```

`galak-ktor` provides 3 modules:

- `galakModule(ExecutableSchema)` adds the main `/graphql` route for queries/mutations.
- `galakSubscriptionModule(ExecutableSchema)` adds the `/subscription` route for subscriptions.
- `galakSandboxModule(ExecutableSchema)` adds the `/sandbox/index.html` for the online IDE

```kotlin
embeddedServer(Netty, port = 8080) {
  val executableSchema = ServiceExecutableSchemaBuilder().build()
  // /graphql route
  galakModule(executableSchema)
  // /subscription route
  galakSubscriptionModule(executableSchema)
  // /sandbox/index.html route
  galakSandboxModule()
}.start(wait = true)
```
