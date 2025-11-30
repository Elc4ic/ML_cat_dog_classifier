package org.example.ai

import org.example.entities.DataSet
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

fun trainProgress(ai: NeuralNetwork, dataset: DataSet, epochs: Int, batch: Int) {
    with2PointsChart("rr") { x1, x2, y1, y2 ->
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

            if (i % 500 == 0) {
                val percent = abs((abs(midTest.second.last() - aa.first()) / 1f) - 0.5f) * 200
                println("Pre result $i/$epochs: ${aa.first()}, answer=${midTest.second.last()}; accuracy: $percent%")
            }
        }
    }

}

fun main() {
    val x = 64
    val epoch = 300_000
    val batch = 8
//    val dataset = PngDataSet("D:\\Projects\\AItest\\src\\main\\resources\\dataset\\training_set2", x)
//    val ai = NeuralNetwork(x * x, 0.01f)
//    ai.addLayer(Layer(512, "hidden1", Function.RELU))
//    ai.addLayer(Layer(128, "hidden2", Function.RELU))
//    ai.addLayer(Layer(1, "output", Function.SIGMOID))

    val ai2 = NeuralNetwork()
    ai2.initFromFile("D:\\Projects\\AItest\\doLearn.txt")

//    trainProgress(ai2, dataset, epoch, batch)

    val metr = Metrics()
    val creator = ImageCreator()
    val file = File("D:\\Projects\\AItest\\src\\main\\resources\\dataset\\messy")
    val valSet = DataSet(file, 64)

    with2PointsChart("chart") { x1, x2, y1, y2 ->
        val data = valSet.getAll()

        repeat(data.size) { i ->
            data[i]?.let {
                val (test, result) = it
                val pair = ai2.run(test)
                val res = pair.first()

                metr.count(res, result.first())

                if (valSet.cats.size > i) {
                    x1.add(i)
                    y1.add(res)
                } else {
                    x2.add(i)
                    y2.add(res)
                }
            }
        }
    }

    println("accuracy: " + metr.accuracy())
    println("catPrecision: " + metr.catPrecision())
    println("dogPrecision: " + metr.dogPrecision())
    println("catRecall: " + metr.catRecall())
    println("dogRecall: " + metr.dogRecall())
//    repeat(valSet.cats.size + valSet.dogs.size) {
//        val (test, result) = valSet.getPairDog(Random.nextBoolean())
//        val i = creator.createFromData(x, x, test.map { p ->
//            val t = (p * 255).toInt()
//            t or (t shl 8) or (t shl 16)
//        }.toTypedArray())
//        creator.save("cd$it", i)
//        val aa = ai2.run(test)
//    }
//    ai2.save("doLearn2")
//    ai2.getNeuronView("neuron\\test_set")
}