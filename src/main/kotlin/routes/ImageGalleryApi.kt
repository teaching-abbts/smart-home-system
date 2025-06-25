package ch.abbts.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

const val IMAGE_DIRECTORY = "src/main/resources/images"

@Serializable
data class ImageGallery(val imageUrls: List<String>)

fun Application.mapImageGalleryApi() {
    routing {
        val imageGalleryUrl = "/image-gallery"

        get(imageGalleryUrl) {
            val directory = File(IMAGE_DIRECTORY)
            val images = directory.listFiles()
                ?.filter { it.isFile and (it.name.endsWith(".jpg") or it.name.endsWith(".png")) }
                ?: emptyList()

            val imageUrls = images.map { "/image/get/${it.name}" }
            val imageGallery = ImageGallery(imageUrls)

            call.respond(imageGallery)
        }
    }
}
