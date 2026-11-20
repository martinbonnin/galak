package test

import com.apollographql.apollo.execution.ExecutableSchema
import galak.ktor.galakModule
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpQueryTest {
  private val executableSchema = ExecutableSchema.Builder()
      .schema("type Query { foo: String! }")
      .resolver { "bar" }
      .build()

  private fun send(enableHttpQuery: Boolean, method: String, body: String?): HttpResponse<String> {
    val server = embeddedServer(Netty, port = 0, host = "127.0.0.1") {
      galakModule(executableSchema, enableHttpQuery = enableHttpQuery)
    }.start(wait = false)
    try {
      val port = runBlocking { server.engine.resolvedConnectors().first().port }
      val publisher = if (body != null) HttpRequest.BodyPublishers.ofString(body) else HttpRequest.BodyPublishers.noBody()
      val request = HttpRequest.newBuilder(URI("http://127.0.0.1:$port/graphql"))
          .header("content-type", "application/json")
          .method(method, publisher)
          .build()
      return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    } finally {
      server.stop(0, 0)
    }
  }

  @Test
  fun queryEnabled() {
    val response = send(true, "QUERY", "{\"query\":\"{foo}\"}")
    assertEquals(200, response.statusCode())
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", response.body())
  }

  @Test
  fun queryDisabled() {
    val response = send(false, "QUERY", "{\"query\":\"{foo}\"}")
    assertEquals(405, response.statusCode())
  }

  @Test
  fun optionsQueryDisabled() {
    val response = send(false, "OPTIONS", null)
    assertEquals(200, response.statusCode())
    assertEquals("GET, POST", response.headers().firstValue("Allow").orElse(null))
    assertNull(response.headers().firstValue("Accept-Query").orElse(null))
  }
}
