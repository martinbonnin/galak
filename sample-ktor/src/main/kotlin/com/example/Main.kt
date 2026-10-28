package com.example

import galak.ktor.galakModule
import galak.ktor.galakSandboxModule
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        // configures /graphql route
        galakModule(ServiceExecutableSchemaBuilder().build())
        // configures /sandbox/* route
        galakSandboxModule()
    }.start(wait = true)
}