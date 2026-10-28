package galak.spring

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.ExecutionContext
import com.apollographql.apollo.execution.ExecutableSchema
import com.apollographql.apollo.execution.GraphQLRequest
import com.apollographql.apollo.execution.GraphQLResponse
import com.apollographql.apollo.execution.parseAsGraphQLRequest
import com.apollographql.apollo.execution.toExternalValueMap
import galak.*
import okio.Buffer
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*

suspend fun ServerRequest.parseAsGraphQLRequest(): Result<GraphQLRequest> {
  return when (this.method()) {
    HttpMethod.GET -> this.queryParams().toExternalValueMap().fold(
      onSuccess = { it.parseAsGraphQLRequest() },
      onFailure = { Result.failure(it) }
    )
    HttpMethod.QUERY, HttpMethod.POST -> {
      awaitBody<String>().let {
        Buffer().writeUtf8(it).parseAsGraphQLRequest()
      }
    }
    else -> Result.failure(Exception("Unhandled method: ${method()}"))
  }
}

sealed interface OnRequestResult {
  class Error(val status: Int, val message: String) : OnRequestResult
  class Proceed(val executionContext: ExecutionContext) : OnRequestResult
}

fun CoRouterFunctionDsl.galakRoutes(
  executableSchema: ExecutableSchema,
  path: String = "/graphql",
  onRequest: (ServerRequest) -> OnRequestResult = { OnRequestResult.Proceed(ExecutionContext.Empty) },
  onResponse: ServerResponse.BodyBuilder.(GraphQLResponse, ExecutionContext) -> Unit = { _, _ -> },
) {
  (path and RequestPredicates.methods(HttpMethod.GET, HttpMethod.POST, HttpMethod.QUERY)).invoke { serverRequest ->
    val graphqlRequestResult = serverRequest.parseAsGraphQLRequest()
    if (!graphqlRequestResult.isSuccess) {
      return@invoke badRequest().buildAndAwait()
    }
    val context = when (val onRequestResult = onRequest(serverRequest)) {
      is OnRequestResult.Error -> {
        val errorResponse = GraphQLResponse.Builder()
          .errors(listOf(Error.Builder(onRequestResult.message).build()))
          .build()
        return@invoke status(onRequestResult.status).contentType(MediaType.parseMediaType("application/graphql-response+json"))
          .bodyValueAndAwait(errorResponse.toByteArray())
      }
      is OnRequestResult.Proceed -> onRequestResult.executionContext
    }
    val graphQLResponse = executableSchema.execute(graphqlRequestResult.getOrThrow(), context)

    return@invoke ok().contentType(MediaType.parseMediaType("application/graphql-response+json"))
      .apply {
        onResponse(graphQLResponse, context)
      }
      .bodyValueAndAwait(graphQLResponse.toByteArray())
  }
  OPTIONS(path) {
    ok().allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.QUERY)
      .header("Accept-Query", "application/json")
      .buildAndAwait()
  }
}

fun CoRouterFunctionDsl.apolloSandboxRoutes(
  title: String = "API sandbox",
  sandboxPath: String = "/sandbox",
  graphqlPath: String = "/graphql",
) {
  GET("$sandboxPath/index.html") {
    ok().contentType(MediaType.TEXT_HTML).bodyValueAndAwait(sandboxHtml(title, it.uriBuilder().replacePath(graphqlPath).build().toString()))
  }
  GET("$sandboxPath/**") {
    permanentRedirect(it.uriBuilder().replacePath("/sandbox/index.html").build()).buildAndAwait()
  }
}

private fun GraphQLResponse.toByteArray(): ByteArray {
  val buffer = Buffer()
  serialize(buffer)
  return buffer.readByteArray()
}