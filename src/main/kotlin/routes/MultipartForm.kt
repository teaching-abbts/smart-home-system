package ch.abbts.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.utils.io.toByteArray
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

const val IMAGE_UPLOAD_DIRECTORY = "src/main/resources/images"

fun Application.mapMultipartForm() {
    routing {
        val uploadImagePath = "/upload-image"

        get(uploadImagePath) {
            val title = "Multipart Formular"
            val acceptMimeTypes = "image/png, image/jpeg"

            call.respondText(
                """
                <!doctype html>
                <html>
                    <header>
                        <title>$title</title>
                        <style>
                        body {
                            width: 100%;
                        }

                        h2 {
                            text-align: center;
                        }

                        form {
                            width: 300px;
                            margin: 0 auto;
                            padding: 20px;
                            border-radius: 10px;
                            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                            background-color: white;
                        }

                        input[type="text"], input[type="email"] {
                            width: 100%;
                        }

                        input[type="submit"] {
                            margin-top: 5px;
                        }
                        </style>
                    </header>
                    <body>
                        <h2>$title</h2>
                        <form action="$uploadImagePath" method="post" enctype="multipart/form-data">
                        <label for="file">File:</label><br />
                        <input
                            type="file"
                            id="file"
                            name="file"
                            accept="$acceptMimeTypes"
                        /><br />
                        <input type="submit" value="Submit" />
                        </form>
                    </body>
                </html>
                """,
                ContentType.Text.Html,
                HttpStatusCode.OK)
        }

        post(uploadImagePath) {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                println("*** Uploading ${part.contentType}, ${part.name}")

                when (part) {
                    is PartData.FileItem -> {
                    val fileName = part.originalFileName as String
                    println("*** originalFileName: $fileName")

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

            call.respondRedirect(uploadImagePath, permanent = false)
        }
    }
}
