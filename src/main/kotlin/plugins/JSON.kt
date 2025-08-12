package ch.abbts.plugins.json

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.setupJsonContentNegotiation() {
  install(ContentNegotiation) { json() }
}
