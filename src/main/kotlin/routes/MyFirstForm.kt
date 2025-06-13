package ch.abbts.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

fun Application.mapMyFirstForm() {
    routing {
        val sendActionPath = "/senden"

        get("/") {
            val title = "Mein erstes Formular"

            call.respondText(
                """
                    <!doctype html>
                    <html>
                    <head>
                        <title>${title}</title>
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

                        input[type="submit"] {
                            margin-top: 5px;
                        }
                        </style>
                    </head>
                    <body>
                        <h2>${title}</h2>
                        <form action="${sendActionPath}" method="post">
                        <label for="name">Name:</label><br />
                        <input type="text" id="name" name="name" /><br />
                        <label for="email">Email:</label><br />
                        <input type="email" id="email" name="email" /><br />
                        <input type="submit" value="Submit" />
                        </form>
                    </body>
                    </html>
                """,
                ContentType.Text.Html,
                HttpStatusCode.OK)
        }
        post(sendActionPath) {
            // transmit data via POST
            // Content-Type: application/x-www-form-urlencoded
            val formParameters = call.receiveParameters()
            val name = formParameters["name"].toString()
            val email = formParameters["email"].toString()

            val title = "Resultate"

            call.respondText(
                """
                    <!doctype html>
                    <html>
                    <head>
                        <title>${title}</title>
                    </head>
                    <body>
                        <h2>${title}</h2>
                        <ul>
                            <li>name: ${name}</li>
                            <li>email: ${email}</li>
                        </ul>
                    </body>
                    </html>
                """,
                ContentType.Text.Html,
                HttpStatusCode.OK)
        }
    }
}
