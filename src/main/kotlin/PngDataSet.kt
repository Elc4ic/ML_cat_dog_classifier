package org.example

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PngDataSet(path: String, start: Int, end: Int, resolution: Int = 64) {
    var cats =
        Array<Pair<FloatArray, FloatArray>?>(end - start + 1) {
            createPair(
                "$path\\cats\\cat.${start + it}.png",
                0f,
                resolution
            )
        }
    var dogs =
        Array<Pair<FloatArray, FloatArray>?>(end - start + 1) {
            createPair(
                "$path\\dogs\\dog.${start + it}.png",
                1f,
                resolution
            )
        }

    fun getPairDog(isDog: Boolean): Pair<FloatArray, FloatArray> {
        return if (isDog) {
            cats.random()!!
        } else {
            dogs.random()!!
        }
    }

    fun getAll(): Array<Pair<FloatArray, FloatArray>?> {
        return cats + dogs
    }

    fun createPair(path: String, res: Float, resolution: Int): Pair<FloatArray, FloatArray> {
        val imgFile = File(path)

        val img = ImageIO.read(imgFile)

        val W = resolution
        val H = resolution
        val resized = img.getScaledInstance(W, H, java.awt.Image.SCALE_SMOOTH)
        val grayImg = BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY)
        val g = grayImg.createGraphics()
        g.drawImage(resized, 0, 0, null)
        g.dispose()

        val floats = FloatArray(W * H) { 0f }
        var idx = 0
        for (y in 0 until H) {
            for (x in 0 until W) {
                val gray = grayImg.raster.getSample(y, x, 0)
                floats[idx++] = gray / 255f
            }
        }

        return floats to floatArrayOf(res)
    }
}