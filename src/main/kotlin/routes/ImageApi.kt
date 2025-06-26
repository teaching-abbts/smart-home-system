package ch.abbts.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.Application
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.toByteArray
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

fun Application.mapImageApi() {
    routing {
        val imageApiBasePath = "/image"

        get("$imageApiBasePath/get/{imageName}") {
            val imageName = call.parameters["imageName"]
            val file = File("$IMAGE_DIRECTORY/$imageName")

            if (file.exists()) {
                call.respondFile(file)
            }
            else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("$imageApiBasePath/upload") {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                println("*** Uploading ${part.contentType}, ${part.name}")

                when (part) {
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName as String
                        println("*** originalFileName: $fileName")

                        val fileBytes = part.provider().toByteArray()
                        val file = File("$IMAGE_DIRECTORY/$fileName")

                        // Ensure the parent directory exists
                        Files.createDirectories(file.toPath().parent)
                        Files.write(file.toPath(), fileBytes, StandardOpenOption.CREATE)
                    }

                    else -> {
                        // Do nothing for now...
                    }
                }
                part.dispose()
            }

            call.respond(HttpStatusCode.OK)
        }

        delete("$imageApiBasePath/delete/{imageName}") {
            val imageName = call.parameters["imageName"]
            val file = File("$IMAGE_DIRECTORY/$imageName")

            try {
                if (file.exists()) {
                    if (file.delete()) {
                        call.respond(HttpStatusCode.OK)
                    }
                    else {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
                else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            catch (e: Exception) {
                call.respondText(
                    e.localizedMessage,
                    ContentType.Text.Plain,
                    HttpStatusCode.InternalServerError)
            }
        }
    }
}
