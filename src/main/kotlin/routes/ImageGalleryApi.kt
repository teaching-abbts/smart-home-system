package ch.abbts.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

const val IMAGE_DIRECTORY = "src/main/resources/images"

@Serializable
data class Image(val url: String, val name: String)

@Serializable
data class ImageGallery(val images: List<Image>)

fun Application.mapImageGalleryApi() {
    routing {
        val imageGalleryUrl = "/image-gallery"

        get(imageGalleryUrl) {
            val directory = File(IMAGE_DIRECTORY)
            val imageFiles = directory.listFiles()
                ?.filter { it.isFile and (it.name.endsWith(".jpg") or it.name.endsWith(".png")) }
                ?: emptyList()

            val images = imageFiles.map { Image("/image/get/${it.name}", it.nameWithoutExtension) }
            val imageGallery = ImageGallery(images)

            call.respond(imageGallery)
        }
    }
}
