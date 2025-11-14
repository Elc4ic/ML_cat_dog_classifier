package org.example

import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toHTML
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

fun with2PointsChart(block: (MutableList<Int>, MutableList<Int>, MutableList<Float>, MutableList<Float>) -> Unit) {
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
    val file = File("chart.html")
    val html = chart.toHTML()
    file.delete()
    file.createNewFile()
    file.printWriter().use { out ->
        out.print(html)
    }
}


fun trainProgress(ai: NeuralNetwork, dataset: PngDataSet, epochs: Int, batch: Int) {
    with2PointsChart { x1, x2, y1, y2 ->
        var balance = false

        for (i in 0 until epochs) {
            balance = !balance
            val batch = List(batch) { dataset.getPairDog(balance) }
            ai.learnBatch(batch)

            val midTest = dataset.getPairDog(Random.nextBoolean())
            val aa = ai.run(midTest.first)
            if (midTest.second.last() == 1f) {
                y1.add(aa.first())
                x1.add(i)
            } else {
                y2.add(aa.first())
                x2.add(i)
            }

            if (i % 100 == 0) {
                val percent = abs((abs(midTest.second.last() - aa.first()) / 1f) - 0.5f) * 200
                println("Pre result $i/$epochs: ${aa.first()}, answer=${midTest.second.last()}; accuracy: $percent%")
            }
        }
    }

}

fun main() {
    val x = 64
    val epoch = 1000
    val batch = 8
    val dataset = PngDataSet("D:\\Projects\\AItest\\src\\main\\resources\\dataset\\training_set", 1, 4000, x)

//    val ai = NeuralNetwork(x * x, 0.01f)
//    ai.addLayer(Layer(512, "hidden1", Function.RELU))
//    ai.addLayer(Layer(128, "hidden2", Function.RELU))
//    ai.addLayer(Layer(1, "output", Function.SIGMOID))


    val ai2 = NeuralNetwork()
    ai2.initFromFile("D:\\Projects\\AItest\\res1.txt")

//    trainProgress(ai2, dataset, epoch, batch)

    val creator = ImageCreator()
    val valSet = PngDataSet("D:\\Projects\\AItest\\src\\main\\resources\\dataset\\test_set", 4001, 5000, 64)
    with2PointsChart { x1, x2, y1, y2 ->
        val data = valSet.getAll()
        repeat(data.size) { i ->
            data[i]?.let {
                val (test, result) = it
                val aa = ai2.run(test)
                if (valSet.cats.size > i) {
                    x1.add(i)
                    y1.add(aa.first())
                }else{
                    x2.add(i)
                    y2.add(aa.first())
                }
            }
        }
    }
//    repeat(valSet.cats.size + valSet.dogs.size) {
//        val (test, result) = valSet.getPairDog(Random.nextBoolean())
//        val i = creator.createFromData(x, x, test.map { p ->
//            val t = (p * 255).toInt()
//            t or (t shl 8) or (t shl 16)
//        }.toTypedArray())
//        creator.save("cd$it", i)
//        val aa = ai2.run(test)
//    }
//    ai.save("charTest1")
//    ai2.getNeuronView("neuron\\test_set")
}