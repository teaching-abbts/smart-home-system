package ch.abbts.plugins.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.JWTVerifier
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
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*
import kotlinx.serialization.json.Json

const val KEYCLOAK_AUTH_NAME = "keycloak-auth"
const val KEYCLOAK_JWT_AUTH_NAME = "keycloak-jwt"
const val KEYCLOAK_SESSION_NAME = "keycloak-session"

@kotlinx.serialization.Serializable
data class KeycloakSession(
        val userId: String,
        val username: String,
        val email: String?,
        val name: String?,
        val roles: List<String>,
        val accessToken: String,
        val refreshToken: String
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
                                    .getString(),
                    jwksUri =
                            environment
                                    .config
                                    .property("ktor.security.oauth.keycloak.jwksUri")
                                    .getString(),
                    issuer =
                            environment
                                    .config
                                    .property("ktor.security.oauth.keycloak.issuer")
                                    .getString()
            )

    val httpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }

    install(Sessions) {
        cookie<KeycloakSession>("KEYCLOAK_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 3600 // 1 hour
            cookie.httpOnly = true
            cookie.secure = false // Set to true in production with HTTPS
        }
    }

    install(Authentication) {
        // JWT authentication for API endpoints
        jwt(KEYCLOAK_JWT_AUTH_NAME) {
            verifier { token ->
                try {
                    // Get the public key from Keycloak's JWKS endpoint
                    val jwks = httpClient.get(config.jwksUri).body<String>()
                    // For simplicity, we'll use a basic verification approach
                    // In production, you should implement proper JWKS parsing

                    // For now, let's just verify the issuer and basic structure
                    val decodedJWT = JWT.decode(token)
                    if (decodedJWT.issuer == config.issuer) {
                        // Return a simple verifier that accepts the token
                        // Note: This is simplified - in production you should verify with the
                        // actual public key
                        object : JWTVerifier {
                            override fun verify(token: com.auth0.jwt.interfaces.DecodedJWT) = token
                        }
                    } else null
                } catch (e: Exception) {
                    log.error("JWT verification failed", e)
                    null
                }
            }

            validate { credential ->
                try {
                    val payload = credential.payload
                    val userId = payload.subject
                    val username = payload.getClaim("preferred_username").asString()
                    val email = payload.getClaim("email").asString()
                    val name = payload.getClaim("name").asString()
                    val rolesArray = payload.getClaim("roles").asArray(String::class.java)
                    val roles = rolesArray?.toList() ?: emptyList()

                    KeycloakUserPrincipal(
                            userId = userId,
                            username = username,
                            email = email,
                            name = name,
                            roles = roles
                    )
                } catch (e: Exception) {
                    log.error("Failed to validate JWT", e)
                    null
                }
            }
        }

        // Session-based authentication
        session<KeycloakSession>(KEYCLOAK_SESSION_NAME) {
            validate { session ->
                // Validate that the session is still valid
                // You could add token expiration checks here
                KeycloakUserPrincipal(
                        userId = session.userId,
                        username = session.username,
                        email = session.email,
                        name = session.name,
                        roles = session.roles
                )
            }
            challenge { call.respondRedirect("/login") }
        }
    }

    routing {
        // Login endpoint - redirects to Keycloak
        get("/login") {
            val authUrl = buildString {
                append(
                        "${config.authServerUrl}/realms/${config.realm}/protocol/openid-connect/auth"
                )
                append("?client_id=${config.clientId}")
                append("&redirect_uri=${config.redirectUri}")
                append("&response_type=code")
                append("&scope=openid email profile")
                append("&state=${generateNonce()}") // Add state for security
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
                        httpClient.submitForm<TokenResponse>(
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

        // Logout endpoint
        get("/logout") {
            val session = call.sessions.get<KeycloakSession>()
            call.sessions.clear<KeycloakSession>()

            // Optionally, you can also call Keycloak's logout endpoint
            if (session != null) {
                val logoutUrl =
                        "${config.authServerUrl}/realms/${config.realm}/protocol/openid-connect/logout" +
                                "?post_logout_redirect_uri=http://localhost:8080" +
                                "&id_token_hint=${session.accessToken}"
                call.respondRedirect(logoutUrl)
            } else {
                call.respondRedirect("/")
            }
        }

        // Protected routes
        authenticate(KEYCLOAK_SESSION_NAME) {
            get("/profile") {
                val principal = call.principal<KeycloakUserPrincipal>()
                if (principal != null) {
                    call.respondText(
                            """
                        <html>
                        <body>
                            <h1>Welcome to Smart Home System!</h1>
                            <p><strong>User ID:</strong> ${principal.userId}</p>
                            <p><strong>Username:</strong> ${principal.username}</p>
                            <p><strong>Email:</strong> ${principal.email ?: "N/A"}</p>
                            <p><strong>Name:</strong> ${principal.name ?: "N/A"}</p>
                            <p><strong>Roles:</strong> ${principal.roles.joinToString(", ")}</p>
                            <p><a href="/logout">Logout</a></p>
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

        // API endpoint with JWT authentication
        authenticate(KEYCLOAK_JWT_AUTH_NAME) {
            get("/api/user") {
                val principal = call.principal<KeycloakUserPrincipal>()
                if (principal != null) {
                    call.respond(
                            mapOf(
                                    "userId" to principal.userId,
                                    "username" to principal.username,
                                    "email" to principal.email,
                                    "name" to principal.name,
                                    "roles" to principal.roles
                            )
                    )
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}

private fun generateNonce(): String {
    return UUID.randomUUID().toString()
}
