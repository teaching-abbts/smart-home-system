package ch.abbts.plugins.authentication

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Authentication provider names
const val KEYCLOAK_SESSION_NAME = "keycloak-session"

// Data Models
@Serializable
data class KeycloakConfig(
  val authServerUrl: String,
  val realm: String,
  val clientId: String,
  val clientSecret: String,
  val redirectUri: String
)

@Serializable
data class TokenResponse(
  val access_token: String,
  val expires_in: Int,
  val refresh_expires_in: Int,
  val refresh_token: String,
  val token_type: String,
  val session_state: String? = null
)

@Serializable
data class UserInfo(
  val sub: String,
  val email_verified: Boolean? = null,
  val name: String? = null,
  val preferred_username: String? = null,
  val given_name: String? = null,
  val family_name: String? = null,
  val email: String? = null,
  val roles: List<String> = emptyList()
)

@Serializable
data class KeycloakSession(
  val userId: String,
  val username: String,
  val email: String?,
  val name: String?,
  val roles: List<String>,
  val accessToken: String,
  val refreshToken: String,
  val sessionId: String = UUID.randomUUID().toString(),
  val createdAt: Long = System.currentTimeMillis()
)

data class KeycloakUserPrincipal(
  val userId: String,
  val username: String,
  val email: String?,
  val name: String?,
  val roles: List<String>
)

fun Application.setupKeycloakAuthentication() {
  val config =
    KeycloakConfig(
      authServerUrl =
        environment
          .config
          .property("ktor.security.oauth.keycloak.authServerUrl")
          .getString(),
      realm =
        environment
          .config
          .property("ktor.security.oauth.keycloak.realm")
          .getString(),
      clientId =
        environment
          .config
          .property("ktor.security.oauth.keycloak.clientId")
          .getString(),
      clientSecret =
        environment
          .config
          .property("ktor.security.oauth.keycloak.clientSecret")
          .getString(),
      redirectUri =
        environment
          .config
          .property("ktor.security.oauth.keycloak.redirectUri")
          .getString()
    )

  val httpClient =
    HttpClient(CIO) {
      install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

  // Configure session management
  install(Sessions) {
    cookie<KeycloakSession>("KEYCLOAK_SESSION") {
      cookie.path = "/"
      cookie.maxAgeInSeconds = 3600 // 1 hour
      cookie.httpOnly = true
      cookie.secure = false // Set to true in production with HTTPS
      cookie.extensions["SameSite"] = "Lax"
    }
  }

  // Configure authentication
  install(Authentication) {
    session<KeycloakSession>(KEYCLOAK_SESSION_NAME) {
      validate { session ->
        // Validate session is not expired (1 hour)
        val isExpired = (System.currentTimeMillis() - session.createdAt) > 3600000
        if (isExpired) {
          null // Session expired
        } else {
          KeycloakUserPrincipal(
            userId = session.userId,
            username = session.username,
            email = session.email,
            name = session.name,
            roles = session.roles
          )
        }
      }
      challenge { call.respondRedirect(LOGIN_URL) }
    }
  }

  routing {
    // Login endpoint - redirects to Keycloak
    get(LOGIN_URL) {
      val state = generateNonce()
      val authUrl = buildString {
        append(
          "${config.authServerUrl}/realms/${config.realm}/protocol/openid-connect/auth"
        )
        append("?client_id=${config.clientId}")
        append("&redirect_uri=${config.redirectUri}")
        append("&response_type=code")
        append("&scope=openid email profile")
        append("&state=$state")
      }
      call.respondRedirect(authUrl)
    }

    // Callback endpoint - handles the authorization code
    get("/auth/callback") {
      val code = call.request.queryParameters["code"]
      val state = call.request.queryParameters["state"]

      if (code == null) {
        call.respond(HttpStatusCode.BadRequest, "Authorization code not found")
        return@get
      }

      try {
        // Exchange authorization code for tokens
        val tokenResponse =
          httpClient
            .submitForm(
              url =
                "${config.authServerUrl}/realms/${config.realm}/protocol/openid-connect/token",
              formParameters =
                parameters {
                  append("grant_type", "authorization_code")
                  append("client_id", config.clientId)
                  append("client_secret", config.clientSecret)
                  append("code", code)
                  append("redirect_uri", config.redirectUri)
                }
            )
            .body<TokenResponse>()

        // Get user info using the access token
        val userInfo =
          httpClient
            .get(
              "${config.authServerUrl}/realms/${config.realm}/protocol/openid-connect/userinfo"
            ) {
              header("Authorization", "Bearer ${tokenResponse.access_token}")
            }
            .body<UserInfo>()

        // Create session
        val session =
          KeycloakSession(
            userId = userInfo.sub,
            username = userInfo.preferred_username ?: "unknown",
            email = userInfo.email,
            name = userInfo.name,
            roles = userInfo.roles,
            accessToken = tokenResponse.access_token,
            refreshToken = tokenResponse.refresh_token
          )

        call.sessions.set(session)
        call.respondRedirect("/profile") // Redirect to a protected page
      } catch (e: Exception) {
        log.error("Failed to process OAuth callback", e)
        call.respond(HttpStatusCode.InternalServerError, "Authentication failed")
      }
    }

    // Logout endpoint with proper Keycloak logout
    get("/logout") {
      val session = call.sessions.get<KeycloakSession>()
      call.sessions.clear<KeycloakSession>()

      // If we have a session, perform Keycloak logout
      if (session != null) {
        try {
          // Call Keycloak logout endpoint to invalidate the session server-side
          val logoutUrl =
            "${config.authServerUrl}/realms/${config.realm}/protocol/openid-connect/logout" +
              "?post_logout_redirect_uri=http://localhost:8080" +
              "&refresh_token=${session.refreshToken}"
          call.respondRedirect(logoutUrl)
        } catch (e: Exception) {
          log.warn("Failed to logout from Keycloak", e)
          call.respondRedirect("/")
        }
      } else {
        call.respondRedirect("/")
      }
    }

    // Protected profile page
    authenticate(KEYCLOAK_SESSION_NAME) {
      get("/profile") {
        val principal = call.principal<KeycloakUserPrincipal>()
        if (principal != null) {
          call.respondText(
            """
                        <html>
                        <head>
                            <title>Smart Home System - Profile</title>
                            <style>
                                body { font-family: Arial, sans-serif; margin: 40px; }
                                .container { max-width: 600px; margin: 0 auto; }
                                .info { background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0; }
                                .roles { background: #e3f2fd; padding: 10px; border-radius: 4px; }
                                a { color: #1976d2; text-decoration: none; }
                                a:hover { text-decoration: underline; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <h1>🏠 Smart Home System</h1>
                                <h2>Welcome, ${principal.name ?: principal.username}!</h2>

                                <div class="info">
                                    <p><strong>User ID:</strong> ${principal.userId}</p>
                                    <p><strong>Username:</strong> ${principal.username}</p>
                                    <p><strong>Email:</strong> ${principal.email ?: "N/A"}</p>
                                    <p><strong>Full Name:</strong> ${principal.name ?: "N/A"}</p>
                                    <div class="roles">
                                        <strong>Roles:</strong> ${
              principal.roles.joinToString(", ").ifEmpty { "No roles assigned" }
            }
                                    </div>
                                </div>

                                <p>
                                    <a href="/user-info">View User Info (JSON)</a> |
                                    <a href="/logout">Logout</a>
                                </p>
                            </div>
                        </body>
                        </html>
                    """.trimIndent(),
            ContentType.Text.Html
          )
        } else {
          call.respondRedirect("/login")
        }
      }
    }

    // Session info endpoint for debugging
    authenticate(KEYCLOAK_SESSION_NAME) {
      get("/session-info") {
        val session = call.sessions.get<KeycloakSession>()
        if (session != null) {
          val sessionAge = (System.currentTimeMillis() - session.createdAt) / 1000
          call.respond(
            mapOf(
              "sessionId" to session.sessionId,
              "userId" to session.userId,
              "username" to session.username,
              "sessionAgeSeconds" to sessionAge,
              "maxAgeSeconds" to 3600
            )
          )
        } else {
          call.respond(HttpStatusCode.Unauthorized, "No active session")
        }
      }
    }
  }
}

private fun generateNonce(): String {
  return UUID.randomUUID().toString()
}
