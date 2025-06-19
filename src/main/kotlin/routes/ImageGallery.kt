package ch.abbts.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.utils.io.toByteArray
import java.io.*
import java.nio.file.*

fun Application.mapImageGallery() {
    routing {
        val imageGalleryUrl = "/image-gallery"
        val imageUploadFormUrl = "$imageGalleryUrl/upload"
        val deleteImageUrlBase = "$imageGalleryUrl/delete"

        fun renderImages(): String {
            val directory = File(IMAGE_UPLOAD_DIRECTORY)
            val images = directory.listFiles()
                ?.filter { it.isFile and (it.name.endsWith(".jpg") or it.name.endsWith(".png")) }
                ?: emptyList()

            var response = ""
            for ((index, image) in images.withIndex()) {
                response +=
                    """
                    <div style="background-image: url(/image/${image.name});" class="image">
                        <a href="$deleteImageUrlBase/${image.name}">Löschen</a>
                    </div>
                    """

                if ((index + 1) % 3 == 0) {
                    response += "<br />"
                }
            }

            return response
        }

        get("/image/{imageName}") {
            val imageName = call.parameters["imageName"]
            call.respondFile(File("$IMAGE_UPLOAD_DIRECTORY/$imageName"))
        }

        get(imageGalleryUrl) {
            call.respondText(
                """
                <!doctype html>
                <html>
                    <header>
                        <title>Image Gallery</title>
                        <style>
                            .image {
                                margin: 0;
                                padding: 0;
                                background-size: cover;
                                display: inline-block;
                                height: 300px;
                                width: 300px;
                            }
                        </style>
                    </header>
                    <body>
                        <h1>Fotoalbum 📸</h1>
                        <p>
                            <a href="$imageUploadFormUrl">Upload</a>
                        </p>
                        ${renderImages()}
                    </body>
                </html>
                """,
                ContentType.Text.Html,
                HttpStatusCode.OK
            )
        }

        get(imageUploadFormUrl) {
            val title = "Image Uplaod"
            val acceptMimeTypes = "image/png, image/jpeg"

            call.respondText(
                """
                <!doctype html>
                <html>
                    <header>
                        <title>$title</title>
                    </header>
                    <body>
                        <h2>$title</h2>
                        <p>
                            <a href="$imageGalleryUrl">Zurück zur Gallerie</a>
                        </p>
                        <form action="$imageUploadFormUrl" method="post" enctype="multipart/form-data">
                        <label for="file">File:</label><br />
                        <input
                            type="file"
                            id="file"
                            name="file"
                            accept="$acceptMimeTypes"
                            multiple
                        /><br />
                        <input type="submit" value="Submit" />
                        </form>
                    </body>
                </html>
                """,
                ContentType.Text.Html,
                HttpStatusCode.OK
            )
        }

        post(imageUploadFormUrl) {
            // transmit data via POST
            // Content-Type: multipart/form-data
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName as String
                        val fileBytes = part.provider().toByteArray()
                        val file = File("$IMAGE_UPLOAD_DIRECTORY/$fileName")
                        // Ensure the parent directory exists
                        Files.createDirectories(file.toPath().parent)
                        Files.write(file.toPath(), fileBytes, StandardOpenOption.CREATE)
                    }
                    else -> {}
                }
                part.dispose()
            }

            call.respondRedirect(imageGalleryUrl, permanent = false)
        }

        get("$deleteImageUrlBase/{imageName}") {
            val imageName = call.parameters["imageName"]
            val file = File("$IMAGE_UPLOAD_DIRECTORY/$imageName")

            file.delete()

            call.respondRedirect(imageGalleryUrl, permanent = false)
        }
    }
}
