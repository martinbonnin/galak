package com.example

import galak.http4k.galakHandler
import org.http4k.server.Netty
import org.http4k.server.asServer


fun main() {
  val executableSchema = ServiceExecutableSchemaBuilder().build()
  galakHandler(executableSchema)
    .asServer(Netty(8000))
    .start()
    .block()
}