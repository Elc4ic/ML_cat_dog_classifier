package org.example.entities

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class DataSet(file: File, resolution: Int = 64) {

    var fileC = file.listFiles().first { file -> file.name.contains("cats") }
    var fileD = file.listFiles().first { file -> file.name.contains("dogs") }

    var fileListC = fileC.listFiles() ?: emptyArray<File>()
    var fileListD = fileD.listFiles() ?: emptyArray<File>()
    var cats =
        Array<Pair<FloatArray, FloatArray>?>(fileListC.size) {
            ImageUtils.createPair(
                fileListC[it],
                0f,
                resolution
            )
        }
    var dogs =
        Array<Pair<FloatArray, FloatArray>?>(fileListD.size) {
            ImageUtils.createPair(
                fileListD[it],
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
}

object DataSetUtils {

}

object ImageUtils {
    fun resizeImage(file: File, resolution: Int): FloatArray {
        val img = ImageIO.read(file)
        val W = resolution
        val H = resolution
        val resized = img.getScaledInstance(W, H, Image.SCALE_SMOOTH)
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
        return floats
    }

    fun createPair(imgFile: File, res: Float, resolution: Int): Pair<FloatArray, FloatArray> {
        val floats = resizeImage(imgFile, resolution)
        return floats to floatArrayOf(res)
    }
}