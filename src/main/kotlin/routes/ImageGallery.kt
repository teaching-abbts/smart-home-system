package ch.abbts.routes

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import java.io.*

fun Application.mapImageGallery() {
    routing {
        get("/image/{imageName}") {
            val imageName = call.parameters["imageName"]
            call.respondFile(File("$IMAGE_UPLOAD_DIRECTORY/$imageName"))
        }
    }
}