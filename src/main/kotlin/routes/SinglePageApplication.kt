package ch.abbts.routes

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Application.mapSinglePageApplication() {
  val isDev = environment.config.propertyOrNull("ktor.development")?.getString()?.toBoolean() ?: false
  val vueDevServerUrl = environment.config.propertyOrNull("vue.devServerUrl")?.getString() ?: "https://127.0.0.1:5173"

  routing {
    if (isDev) {
      // In development mode, proxy requests to Vue dev server
      setupVueDevProxy(vueDevServerUrl)
    } else {
      // In production mode, serve built Vue app
      singlePageApplication {
        vue("src/main/vue-project/dist")
      }
    }
  }
}

private fun Route.setupVueDevProxy(vueDevServerUrl: String) {
  val client = HttpClient(CIO) {
    engine {
      https {
        trustManager = object : javax.net.ssl.X509TrustManager {
          override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
          override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
          override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        }
      }
    }
  }

  // Proxy all static assets and API calls that don't conflict with backend routes
  get("/{path...}") {
    val path = call.parameters.getAll("path")?.joinToString("/") ?: ""

    // Skip proxying for backend API routes (adjust these patterns as needed)
    if (path.startsWith("api/") || path.startsWith("auth/")) {
      return@get
    }

    try {
      val url = if (path.isEmpty()) vueDevServerUrl else "$vueDevServerUrl/$path"
      val queryString = call.request.queryString()
      val fullUrl = if (queryString.isNotEmpty()) "$url?$queryString" else url

      val response = client.get(fullUrl) {
        // Forward original headers (except host)
        call.request.headers.forEach { name, values ->
          if (name.lowercase() !in listOf("host", "content-length")) {
            values.forEach { value ->
              header(name, value)
            }
          }
        }
      }

      // Forward response headers
      response.headers.forEach { name, values ->
        if (name.lowercase() !in listOf("transfer-encoding", "content-encoding")) {
          values.forEach { value ->
            call.response.header(name, value)
          }
        }
      }

      call.respond(response.status, response.bodyAsChannel())
    } catch (e: Exception) {
      // If Vue dev server is not available, return 404
      call.respond(HttpStatusCode.NotFound, "Vue dev server not available: ${e.message}")
    }
  }
}
