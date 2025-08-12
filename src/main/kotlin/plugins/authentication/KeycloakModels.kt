package ch.abbts.plugins.authentication

import kotlinx.serialization.Serializable

@Serializable
data class KeycloakConfig(
        val authServerUrl: String,
        val realm: String,
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String,
        val jwksUri: String,
        val issuer: String
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

data class KeycloakUserPrincipal(
        val userId: String,
        val username: String,
        val email: String?,
        val name: String?,
        val roles: List<String>
) : io.ktor.server.auth.Principal
