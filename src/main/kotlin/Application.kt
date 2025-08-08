package ch.abbts

import ch.abbts.plugins.authentication.*
import ch.abbts.plugins.http.*
import ch.abbts.routes.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.myModule() {
    install(ContentNegotiation) {
        json()
    }

    // https://ktor.io/docs/server-cors.html
    install(CORS) {
        anyHost()
    }
    setupHttpsRedirect()
    setupSessionAuthenticationWithRouting()

    mapUserInfoRoutes()
    mapImageApi()
    mapImageGalleryApi()
    mapSinglePageApplication()
}
