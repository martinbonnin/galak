package test

import com.apollographql.apollo.execution.ExecutableSchema
import galak.http4k.galakHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.query
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleTest {
  private val executableSchema = ExecutableSchema.Builder()
      .schema(
          """
            type Query {
                foo: String!
            }
          """.trimIndent()
      )
      .resolver {
        "bar"
      }
      .build()

  @Test
  fun simpleTest() {
    val uri = Uri.of("https://example.com/graphql").query("query", "{foo}")
    val response = galakHandler(executableSchema)(Request(Method.GET, uri))

    val responseText = response.body.stream.reader().readText()
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", responseText)
  }

  @Test
  fun plusInQueryString() {
    // Browsers encode spaces as '+' in query strings (application/x-www-form-urlencoded)
    val response = galakHandler(executableSchema)(Request(Method.GET, "https://example.com/graphql?query=query+Q+%7B+foo+%7D"))

    val responseText = response.body.stream.reader().readText()
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", responseText)
  }

  @Test
  fun encodedPlusInQueryString() {
    // A literal '+' is encoded as %2B and must be kept as is
    val response = galakHandler(executableSchema)(Request(Method.GET, "https://example.com/graphql?query=%7Bfoo%7D&variables=%7B%22a%22%3A%22%2B%22%7D"))

    val responseText = response.body.stream.reader().readText()
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", responseText)
  }

  @Test
  fun queryMethod() {
    val request = Request(Method.QUERY, "https://example.com/graphql")
        .header("content-type", "application/json")
        .body("{\"query\":\"{foo}\"}")
    val response = galakHandler(executableSchema)(request)

    val responseText = response.body.stream.reader().readText()
    assertEquals("{\"data\":{\"foo\":\"bar\"}}", responseText)
  }

  @Test
  fun optionsMethod() {
    val response = galakHandler(executableSchema)(Request(Method.OPTIONS, "https://example.com/graphql"))

    assertEquals(Status.OK, response.status)
    assertEquals("GET, POST, QUERY", response.header("Allow"))
    assertEquals("application/json", response.header("Accept-Query"))
  }

  @Test
  fun queryMethodDisabled() {
    val request = Request(Method.QUERY, "https://example.com/graphql")
        .header("content-type", "application/json")
        .body("{\"query\":\"{foo}\"}")
    val response = galakHandler(executableSchema, enableHttpQuery = false)(request)

    assertEquals(Status.METHOD_NOT_ALLOWED, response.status)
  }

  @Test
  fun optionsMethodQueryDisabled() {
    val response = galakHandler(executableSchema, enableHttpQuery = false)(Request(Method.OPTIONS, "https://example.com/graphql"))

    assertEquals(Status.OK, response.status)
    assertEquals("GET, POST", response.header("Allow"))
    assertEquals(null, response.header("Accept-Query"))
  }
}
