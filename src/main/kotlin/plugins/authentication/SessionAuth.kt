package ch.abbts.plugins.authentication

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val name: String)

data class LoginModel(val postUrl: String, val userParamName: String, val passwordParamName: String)

const val AUTH_FORM_NAME:String = "auth_form"
const val AUTH_SESSION_NAME:String = "auth_session"
const val LOGIN_URL:String = "/login"
const val PASSWORD_PARAM_NAME:String = "password"
const val USER_PARAM_NAME:String = "username"
const val USER_SESSION_COOKIE_MAX_AGE_SECONDS:Long = 60
const val USER_SESSION_COOKIE_NAME:String = "user_session"
const val USER_SESSION_COOKIE_PATH:String = "/"

private fun authenticate(credential: UserPasswordCredential): UserIdPrincipal? {
    val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }
    val usernameToPasswordMap = mutableMapOf<String, ByteArray>()

    usernameToPasswordMap["admin"] = digestFunction("top-secret")

    return if (
        usernameToPasswordMap.containsKey(credential.name)
        && usernameToPasswordMap[credential.name].contentEquals(digestFunction(credential.password))
    ) {
        UserIdPrincipal(credential.name)
    } else {
        null
    }
}

fun Application.setupSessionAuthenticationWithRouting() {
    install(Sessions) {
        cookie<UserSession>(USER_SESSION_COOKIE_NAME) {
            cookie.path = USER_SESSION_COOKIE_PATH
            cookie.maxAgeInSeconds = USER_SESSION_COOKIE_MAX_AGE_SECONDS
        }
    }

    install(Authentication) {
        // Handle form-based authentication requests
        form(AUTH_FORM_NAME) {
            userParamName = USER_PARAM_NAME
            passwordParamName = PASSWORD_PARAM_NAME

            validate { credential ->
                authenticate(credential)
            }
            challenge {
                call.respondRedirect(LOGIN_URL)
            }
        }
        // Handle session-based authentication requests
        session<UserSession>(AUTH_SESSION_NAME) {
            validate { session ->
                session
            }
            challenge {
                call.respondRedirect(LOGIN_URL)
            }
        }
    }

    routing {
        // GET /login
        // Allow anonymous users to access the login page
        get(LOGIN_URL) {
            val loginModel = LoginModel(
                postUrl = LOGIN_URL,
                userParamName = USER_PARAM_NAME,
                passwordParamName = PASSWORD_PARAM_NAME
            )
            call.respondText(
                """
                <html lang="de">
                <body>
                    <form action="${LOGIN_URL}" enctype="application/x-www-form-urlencoded" method="post">
                    <p>
                        <label for="username">Username:</label>
                        <input id="username" type="text" name="${USER_PARAM_NAME}" />
                    </p>
                    <p>
                        <label for="password">Password:</label>
                        <input id="password" type="password" name="${PASSWORD_PARAM_NAME}" />
                    </p>
                    <p>
                        <input type="submit" value="Login" />
                    </p>
                    </form>
                </body>
                </html>
                """,
                ContentType.Text.Html
            )
        }

        val ROUTE_SESSION_INFO = "/session-info"

        // Protected endpoints with form-based authentication
        authenticate(AUTH_FORM_NAME) {
            // POST /login
            post(LOGIN_URL) {
                val principal = call.principal<UserIdPrincipal>();
                val userName = principal?.name.toString()
                val userSession = UserSession(name = userName)
                call.sessions.set(userSession)

                // Authentication successful, redirecting to home
                call.respondRedirect(ROUTE_SESSION_INFO)
            }
        }

        // Protected endpoint that requires session authentication
        authenticate(AUTH_SESSION_NAME) {
            get(ROUTE_SESSION_INFO) {
                val userSession = call.sessions.get<UserSession>()

                // Check if the user session exists
                if (userSession != null) {
                    call.respondText("Welcome, ${userSession.name}!")
                } else {
                    call.respondRedirect(LOGIN_URL)
                }
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect(LOGIN_URL)
        }
    }
}
