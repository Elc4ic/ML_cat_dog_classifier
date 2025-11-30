package org.example.ai

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import kotlin.random.Random
import kotlin.system.exitProcess

class ImageCreator {

    fun createImage(width: Int, height: Int, draw: (Graphics2D) -> Unit): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        try {
            g.color = Color.WHITE
            g.fillRect(0, 0, width, height)

            draw(g)
        } finally {
            g.dispose()
        }
        return img
    }

    private fun saveJpeg(image: BufferedImage, file: File, quality: Float = 0.9f) {
        require(quality in 0.0f..1.0f) { "quality must be between 0.0 and 1.0" }

        val writers = ImageIO.getImageWritersByFormatName("jpeg")
        if (!writers.hasNext()) throw RuntimeException("No JPEG ImageWriter found")
        val writer: ImageWriter = writers.next()

        FileImageOutputStream(file).use { ios ->
            writer.output = ios
            val param: ImageWriteParam = writer.defaultWriteParam
            if (param.canWriteCompressed()) {
                param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                param.compressionQuality = quality
            }
            val iioImage = IIOImage(image, null, null)
            writer.write(null, iioImage, param)
            writer.dispose()
        }
    }

    fun createFromData(w: Int, h: Int, data: Array<Int>): BufferedImage {
        try {
            val imgFromPixels = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    imgFromPixels.setRGB(x, y, data[x * w + y])
                }
            }
            return imgFromPixels
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
    }

    fun save(name: String, image: BufferedImage) {
        saveJpeg(image, File("$name.jpg"), quality = 0.9f)
        println("Wrote: ${File("$name.jpg").absolutePath}")
    }

    fun createDataSet(n: Int, w: Int, h: Int): List<Pair<Array<Float>, Array<Float>>> {
        return List(n) {
            val input = Array(1) { Random.nextFloat() }
            val output = Array(w * h) { i ->
                (i).toFloat()
            }
            println(output.joinToString())
            input to output
        }
    }
}
//            val data = createDemoImage(n.toString(), w, h).data.getPixels(0, 0, w, h, output)