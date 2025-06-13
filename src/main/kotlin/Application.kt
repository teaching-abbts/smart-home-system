package ch.abbts

import io.ktor.server.application.*
import io.ktor.server.netty.*
import ch.abbts.routes.*

data class User(val name: String, val email: String)

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.myModule() {
    mapSimpleForm()
    mapMultipartForm()
}
