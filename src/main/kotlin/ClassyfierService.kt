package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.example.ai.ImageCreator
import org.example.ai.Metrics
import org.example.ai.NeuralNetwork
import org.example.entities.DataSet
import org.example.ai.with2PointsChart
import org.slf4j.LoggerFactory

interface ClassyfierService {
    fun initFromFile(path: String)
    fun train(batch: List<Pair<FloatArray, FloatArray>>)
    fun classify(input: FloatArray): Float
    fun test(testData: List<Pair<FloatArray, FloatArray>>)
}

class ClassyfierServiceImpl(
    val ai: NeuralNetwork,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ClassyfierService {

    override fun initFromFile(path: String) {
        ai.initFromFile(path)
    }

    override fun train(batch: List<Pair<FloatArray, FloatArray>>) {
        ai.learnBatch(batch)
        ai.save("train${batch.hashCode()}")
    }

    override fun classify(input: FloatArray): Float {
        val res = ai.run(input).first()
        logger.info("result: $res")
        return res
    }

    override fun test(testData: List<Pair<FloatArray, FloatArray>>) {
        val metr = Metrics()
        val creator = ImageCreator()

        with2PointsChart("chart") { x1, x2, y1, y2 ->

            repeat(testData.size) { i ->
                testData[i].let {
                    val (test, result) = it
                    val pair = ai.run(test)
                    val res = pair.first()

                    metr.count(res, result.first())

                    if (it.second.first() != 0f) {
                        x1.add(i)
                        y1.add(res)
                    } else {
                        x2.add(i)
                        y2.add(res)
                    }
                }
            }
        }

        logger.info("accuracy: " + metr.accuracy())
        logger.info("catPrecision: " + metr.catPrecision())
        logger.info("dogPrecision: " + metr.dogPrecision())
        logger.info("catRecall: " + metr.catRecall())
        logger.info("dogRecall: " + metr.dogRecall())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClassyfierService::class.java)
    }
}