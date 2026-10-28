---
title: "Apollo Usage Reporting"
---

Apollo supports sending [operation and field reports](https://www.apollographql.com/docs/graphos/platform/insights/sending-operation-metrics) to GraphOS.

:::note
If you have a federated graph, see [ftv1](/galak/federation/#tracing-ftv1) instead for how to send tracing information to the router.
:::

## Sending usage reports

Operation and field reports are sent using a protobuf protocol to `https://usage-reporting.api.apollographql.com/api/ingress/traces`.

Apollo collects, encodes and sends those reports using the `galak-apollo-reporting` artifact:

```kotlin
dependencies {
  // Add the Apollo monograph artifact
  implementation("net.mbonnin.galak:galak-apollo-reporting:%latest_version%")
}
```

To enable reports, configure your `ExecutableSchema` with a `ApolloReportsInstrumentation` and matching `ApolloReportsContext`:

```kotlin
// Get your API key from the environment or secret manager
val apolloKey = System.getenv("APOLLO_KEY")

// Install the ApolloReportsInstrumentation in the executable schema
val schema = ServiceExecutableSchemaBuilder()
    .addInstrumentation(ApolloReportsInstrumentation(apolloKey))
    .build()

// Create a new ApolloReportsContext() for each operation and use it through execution
val reportsContext = ApolloReportsContext()
val response = schema.execute(request, reportsContext)

// The reports are sent in the background
```
