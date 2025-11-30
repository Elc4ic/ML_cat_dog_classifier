package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.readByte
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.fileInput
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.p
import kotlinx.html.submitInput
import kotlinx.html.title
import kotlinx.io.readByteArray
import org.example.entities.ImageUtils
import org.example.ai.NeuralNetwork
import org.example.entities.DataSet
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.abs
import kotlin.random.Random
import kotlin.text.toLong

@OptIn(InternalAPI::class)
fun main() {
    embeddedServer(CIO, port = 9090, host = "0.0.0.0") {

        val resolution = 64
        val epoch = 300_000
        val batch = 8
        val ai = NeuralNetwork()
        ai.initFromFile("doLearn.txt")
        val classService = ClassyfierServiceImpl(ai)

        val resPath = "src/main/resources/images"
        val dir = File(resPath)
        val path = resPath + "/test.png"

        routing {

            get("/avatar") {
                val file = File(path)
                val avatarExists = file.exists()
                call.respondHtml {
                    body {
                        h2 { +"Upload avatar" }

                        form(
                            action = "/save",
                            encType = FormEncType.multipartFormData,
                            method = FormMethod.post
                        ) {
                            fileInput {
                                name = "test"
                            }
                            submitInput { value = "Upload" }
                        }

                        if (avatarExists) {
                            val bites = ImageUtils.resizeImage(file, resolution)
                            val res = classService.classify(bites)
                            h3 { +"Your image + result:" }
                            img(src = path) {
                                width = "200"
                            }
                            val isDog = res > 0.5f
                            val p = abs((abs((if (isDog) 1 else 0) - res) / 1f) - 0.5f) * 200
                            p { +"it's ${if (isDog) "dog" else "cat"} on ${p}%" }
                        }
                    }
                }
            }

            post("/save") {
                val multipart = call.receiveMultipart()

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "test") {
                        val bytes = part.provider().readBuffer.readByteArray()
                        File(path).writeBytes(bytes)
                    }
                    part.dispose()
                }

                call.respondRedirect("/avatar")
            }

            post("/classify") {
                val img = call.receive<File>()
                val floats = ImageUtils.resizeImage(img, resolution)
                val res = classService.classify(floats)
                call.respond(HttpStatusCode.Created, res)
            }

            post("/train") {
                val file = call.receive<File>()
                val dataset = DataSet(file, resolution)
                var balance = false
                for (i in 0 until epoch) {
                    balance = !balance
                    val batch = List(batch) { dataset.getPairDog(balance) }
                    ai.learnBatch(batch)

                    val midTest = dataset.getPairDog(Random.nextBoolean())
                    val aa = ai.run(midTest.first)

                    if (i % 500 == 0) {
                        val percent = abs((abs(midTest.second.last() - aa.first()) / 1f) - 0.5f) * 200
                        println("Pre result $i/$epoch: ${aa.first()}, answer=${midTest.second.last()}; accuracy: $percent%")
                    }
                }
            }
            staticFiles(resPath, dir)
        }

    }.start(wait = true)
}
