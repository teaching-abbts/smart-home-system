package ch.abbts.routes

import io.ktor.server.application.Application
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.http.content.vue
import io.ktor.server.routing.routing

fun Application.mapSinglePageApplication() {
    routing {
        singlePageApplication {
            vue("src/main/vue-project/dist")
        }
    }
}
