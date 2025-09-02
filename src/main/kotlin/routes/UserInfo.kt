package ch.abbts.routes

import ch.abbts.*
import ch.abbts.plugins.authentication.keycloak.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
  val name: String,
  val email: String? = null,
  val username: String? = null,
  val roles: List<String> = emptyList()
)

fun Application.mapUserInfoRoutes() {
  routing {
    // Protected endpoints that require the user to have a valid Keycloak session
    // authenticate(KEYCLOAK_SESSION_NAME) {
    get("/user-info") {
      val principal = call.principal<KeycloakUserPrincipal>()
      val userInfo =
        UserInfoResponse(
          name = principal?.name ?: "Unknown",
          email = principal?.email,
          username = principal?.username,
          roles = principal?.roles ?: emptyList()
        )

      call.respond(userInfo)
    }
    // }
  }
}
