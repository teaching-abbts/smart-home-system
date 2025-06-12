package ch.abbts

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.myModule() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
