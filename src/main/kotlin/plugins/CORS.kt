package ch.abbts.plugins.cors

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.setupCORS() {
  install(CORS) {
    anyHost()
    allowMethod(HttpMethod.Options)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
  }
}
