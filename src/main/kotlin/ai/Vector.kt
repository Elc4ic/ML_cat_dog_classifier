package org.example.ai

import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toHTML
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import java.io.File

object Vector {
    fun dot(v1: FloatArray, v2: FloatArray): Float {
        return v1.zip(v2) { f1, f2 -> f1 * f2 }.sum()
    }

}

class Metrics {
    private var cat = 0
    private var dog = 0
    private var ncat = 0
    private var ndog = 0
    private var EPSILON = 0.4f
    private val MID = 0.5f

    fun setEpsilon(eps: Float) {
        EPSILON = eps
    }

    fun count(res: Float, answer: Float) {
        if (answer == 1.0f) {
            if (res > MID && answer - EPSILON < res) dog++
            else ndog++
        }
        if (answer == 0.0f) {
            if (res < MID && EPSILON > res) cat++
            else ncat++
        }
    }

    fun accuracy() = (cat + dog).toFloat() / (cat + dog + ndog + ncat)
    fun catPrecision() = dog.toFloat() / (dog + ndog)
    fun dogPrecision() = cat.toFloat() / (cat + ncat)
    fun catRecall() = cat.toFloat() / (cat + dog)
    fun dogRecall() = dog.toFloat() / (cat + dog)
}

fun with2PointsChart(name: String,block: (MutableList<Int>, MutableList<Int>, MutableList<Float>, MutableList<Float>) -> Unit) {
    val catY = mutableListOf<Float>()
    val dogY = mutableListOf<Float>()
    val epochCatX = mutableListOf<Int>()
    val epochDogX = mutableListOf<Int>()

    block(epochDogX, epochCatX, dogY, catY)
    val catFrame = dataFrameOf(
        "epoch" to epochCatX,
        "value" to catY,
        "type" to List(epochCatX.size) { "cat" }
    )

    val dogFrame = dataFrameOf(
        "epoch" to epochDogX,
        "value" to dogY,
        "type" to List(epochDogX.size) { "dog" }
    )
    val frame = catFrame.concat(dogFrame)

    val chart = frame.plot {
        points {
            x("epoch")
            y("value")
            color("type")
            size = 2.0
        }
        layout {
            title = "Learning plot"
            caption = "See learning rate"
            size = 700 to 450
        }
    }
    val file = File("$name.html")
    val html = chart.toHTML()
    file.delete()
    file.createNewFile()
    file.printWriter().use { out ->
        out.print(html)
    }
}