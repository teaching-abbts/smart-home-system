package ch.abbts

import ch.abbts.plugins.authentication.*
import ch.abbts.plugins.http.*
import ch.abbts.plugins.json.*
import ch.abbts.routes.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.myModule() {
    setupJsonContentNegotiation()
    setupHttpsRedirect()
    setupSessionAuthenticationWithRouting()

    mapUserInfoRoutes()
    mapImageApi()
    mapImageGalleryApi()
    mapSinglePageApplication()
}
