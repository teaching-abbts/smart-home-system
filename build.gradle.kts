plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ktor)
}

group = "abb-ts.ch"

version = "0.0.1"

application { mainClass = "io.ktor.server.netty.EngineMain" }

repositories { mavenCentral() }

dependencies {
  implementation(libs.ktor.network.tls.certificates)
  implementation(libs.ktor.serialization.json)
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.config.yaml)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.cors)
  implementation(libs.ktor.server.forwarded.header)
  implementation(libs.ktor.server.http.redirect)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.sessions)
  implementation(libs.logback.classic)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.ktor.server.test.host)
}
