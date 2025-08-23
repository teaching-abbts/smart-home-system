group = "abb-ts.ch"

version = "0.0.1"

application { mainClass = "io.ktor.server.netty.EngineMain" }

kotlin {
  jvmToolchain(21)
}

repositories {
  gradlePluginPortal()
  google()
  mavenCentral()
}

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ktor)
}

dependencies {
  implementation(libs.ktor.network.tls.certificates)
  implementation(libs.ktor.serialization.json)
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.config.yaml)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.http.redirect)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.sessions)
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.cio)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.logback.classic)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.ktor.server.test.host)
}

data class CommandLineConfig(val cmd: String, val windowsCmd: String? = null)

fun Exec.runCommandLine(commandLineConfig: CommandLineConfig) {
  val isWindows = System.getProperty("os.name").lowercase().contains("windows")

  if (isWindows) {
    commandLine("cmd.exe", "/C", commandLineConfig.windowsCmd ?: commandLineConfig.cmd)
  } else {
    commandLine(commandLineConfig.cmd.splitToSequence(' ').toList())
  }
}

fun Exec.runCommandLine(vararg arguments: String) {
  runCommandLine(CommandLineConfig(arguments.joinToString(" ")))
}

fun Exec.runPnpmCommand(vararg npmArguments: String) {
  workingDir = File("src/main/vue-project")

  runCommandLine("pnpm " + npmArguments.joinToString(" "))
}

tasks.register<Exec>("install-vue") {
  group = "smart-home-system"
  description = "installs all the npm packages for the the vue-project."

  runPnpmCommand("install")
}

tasks.register<Exec>("build-vue") {
  group = "smart-home-system"
  description = "builds the vue-project."

  dependsOn("install-vue")
  runPnpmCommand("run", "build")
}

// Make the build task depend on build-vue
tasks.named("build") {
  dependsOn("build-vue")
}
