package ch.abbts

import io.ktor.server.application.*
import io.ktor.server.netty.*
import ch.abbts.routes.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.myModule() {
    install(ContentNegotiation) {
        json()
    }
    mapImageApi()
    mapImageGalleryApi()
}
