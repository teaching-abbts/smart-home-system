package ch.abbts

import ch.abbts.plugins.authentication.keycloak.*
import ch.abbts.plugins.cors.*
import ch.abbts.plugins.http.*
import ch.abbts.plugins.json.*
import ch.abbts.routes.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

const val KEYCLOAK_SESSION_NAME = "keycloak-session"

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.myModule() {
  setupCORS()
  setupJsonContentNegotiation()
  setupHttpsRedirect()
  setupKeycloakAuthentication()

  mapUserInfoRoutes()
  mapImageApi()
  mapImageGalleryApi()
  mapSinglePageApplication()
}
