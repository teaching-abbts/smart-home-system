package ch.abbts

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.netty.*
import io.ktor.server.html.*
import io.ktor.http.HttpStatusCode
import kotlinx.html.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.myModule() {
    routing {
        get("/") {
            val title = "Mein erstes Formular"

            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title {
                        +title
                    }
                }
                body {
                    h2 {
                        +title
                    }
                    form(action = "/senden", method = FormMethod.post) {
                        label("name") {
                            +"Name:"
                        }
                        textInput(name="name") {}
                        submitInput() {
                            value = "Senden"
                        }
                    }
                }
            }
        }
    }
}
