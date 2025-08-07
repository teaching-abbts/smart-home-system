package ch.abbts.routes

import ch.abbts.plugins.authentication.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(val name: String, val email: String? = null)

fun Application.mapUserInfoRoutes() {
    routing {
        // Protected endpoints that require the user to have a valid session
        authenticate(AUTH_SESSION_NAME) {
            get("/user-info") {
                val userSession = call.sessions.get<UserSession>()
                val userInfo = UserInfo(name = userSession?.name ?: "Unknown")

                call.respond(userInfo)
            }
        }
    }
}
