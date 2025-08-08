package ch.abbts.plugins.http

import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.httpsredirect.*
import java.io.File

fun Application.setupHttpsRedirect() {
  install(HttpsRedirect) {
    sslPort = 8443
    permanentRedirect = false
  }
}

// fun Application.configureHttps(hostBinding: String, port: Int) {
//   val keyStoreFile = File("build/keystore.jks")
//   val keyStore = buildKeyStore {
//     certificate("sampleAlias") {
//       password = "foobar"
//       domains = listOf("127.0.0.1", hostBinding, "localhost")
//     }
//   }
//   keyStore.saveToFile(keyStoreFile, "123456")

//   sslConnector(
//     keyStore = keyStore,
//     keyAlias = "sampleAlias",
//     keyStorePassword = { "123456".toCharArray() },
//     privateKeyPassword = { "foobar".toCharArray() }) {
//     this.port = port
//     keyStorePath = keyStoreFile
//   }
// }
