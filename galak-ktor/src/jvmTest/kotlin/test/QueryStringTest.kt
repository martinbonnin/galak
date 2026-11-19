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

class QueryStringTest {
  private val executableSchema = ExecutableSchema.Builder()
      .schema("type Query { foo: String! }")
      .resolver { "bar" }
      .build()

  private fun get(query: String): String {
    val server = embeddedServer(Netty, port = 0, host = "127.0.0.1") {
      galakModule(executableSchema)
    }.start(wait = false)
    try {
      val port = runBlocking { server.engine.resolvedConnectors().first().port }
      val request = HttpRequest.newBuilder(URI("http://127.0.0.1:$port/graphql?$query")).GET().build()
      return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body()
    } finally {
      server.stop(0, 0)
    }
  }

  @Test
  fun plusInQueryString() {
    // Browsers encode spaces as '+' in query strings (application/x-www-form-urlencoded)
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", get("query=query+Q+%7B+foo+%7D"))
  }

  @Test
  fun encodedPlusInQueryString() {
    // A literal '+' is encoded as %2B and must be kept as is
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", get("query=%7Bfoo%7D&variables=%7B%22a%22%3A%22%2B%22%7D"))
  }
}
