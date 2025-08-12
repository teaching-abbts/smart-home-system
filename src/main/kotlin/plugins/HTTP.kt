package ch.abbts.plugins.http

import io.ktor.server.application.*
import io.ktor.server.plugins.httpsredirect.*

fun Application.setupHttpsRedirect() {
    install(HttpsRedirect) {
        sslPort = 8443
        permanentRedirect = false
    }
}
