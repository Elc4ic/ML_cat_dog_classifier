package org.example.ai

import java.io.File

class NeuralNetwork(var inputSize: Int = 64 * 64, val lr: Float = 0.01f) {
    val layers = mutableListOf<Layer>()

    fun addLayer(layer: Layer) {
        if (layers.isEmpty()) {
            layer.initWeights(inputSize)
        } else {
            layer.initWeights(layers.last().neurons.size)
        }
        layers.add(layer)
    }

    fun run(input: FloatArray): FloatArray {
        var temp = input
        for (layer in layers) {
            temp = layer.calc(temp)
        }
        return temp
    }

    fun learnBatch(batch: List<Pair<FloatArray, FloatArray>>) {
        if (batch.isEmpty()) return

        var accumulatedGradients = _initZeroGradients()

        for ((x, y) in batch) {
            val sampleGradients = _calculateGradientsForSample(x, y)
            accumulatedGradients = _addGradients(accumulatedGradients, sampleGradients)
        }

        val averagedGradients = _averageGradients(accumulatedGradients, batch.size)
        for (i in layers.indices) {
            layers[i].applyGradients(averagedGradients[i], lr)
        }
    }

    fun save(name: String) {
        val file = File("$name.txt")
        file.delete()
        file.createNewFile()
        file.printWriter().use { out ->
            out.println("inputSize:${inputSize}")
            layers.forEach { layer ->
                out.println("layer:${layer.name} ${layer.size} ${layer.f}")
                layer.neurons.forEach { neuron ->
                    out.println(neuron.weight.joinToString("|") + " " + neuron.bias)
                }
            }
        }
    }

    fun initFromFile(path: String) {
        val inSizeSuf = "inputSize:"
        val layerSuf = "layer:"
        val wbSplitter = " "
        val propertiesSplitter = ":"
        val weightSplitter = "|"

        var i = 0
        var layerTemp: Layer? = null

        File(path).forEachLine { line ->
            if (line.startsWith(inSizeSuf)) {
                inputSize = line.split(":").last().toInt()
            } else if (line.startsWith(layerSuf)) {
                layerTemp?.let {
                    layers.add(it)
                }
                val property = line.split(":").last().split(" ")
                val name = property[0]
                val size = property[1].toInt()
                val f = property[2].toFunction()
                layerTemp = Layer(size, name, f)
                i = 0
            } else {
                val (weights, bias) = line.split(" ")
                layerTemp?.neurons[i]?.bias = bias.toFloat()
                layerTemp?.neurons[i]?.weight = weights.split("|").map { it.toFloat() }.toFloatArray()
                i++
            }
        }
        layerTemp?.let { layers.add(it) }
    }

    fun getNeuronView(path: String) {
        val creator = ImageCreator()
        layers.forEachIndexed { il, layer ->
            layer.neurons.forEachIndexed { i, neuron ->
                if (i % 10 == 0) return@forEachIndexed
                val data = neuron.weight
                val min = data.min()
                val max = data.max()
                var size = 1
                while (size * size < data.size) size++
                val image = creator.createFromData(
                    size - 1, size - 1,
                    data.map { f ->
                        val t: Int = ((f - min) / (max - min) * 255f).toInt()
                        t or (t shl 8) or (t shl 16)
                    }.toTypedArray()
                )
                creator.save("$path$il.$i", image)
            }
        }
    }

    private fun _calculateGradientsForSample(x: FloatArray, y: FloatArray): List<LayerGradients> {
        val networkGradients = mutableListOf<LayerGradients>()

        val outputs = mutableListOf<FloatArray>()
        var current = x
        layers.forEach { layer ->
            outputs.add(current)
            current = layer.calc(current)
        }

        var error = FloatArray(y.size) { i -> current[i] - y[i] }
        for (i in layers.indices.reversed()) {
            val inputToLayer = outputs[i]
            val (newError, layerGrads) = layers[i].backward(error, inputToLayer)
            networkGradients.add(0, layerGrads)
            error = newError
        }
        return networkGradients
    }

    private fun _initZeroGradients(): List<LayerGradients> {
        val zeroGradients = mutableListOf<LayerGradients>()
        var currentInputSize = inputSize
        for (layer in layers) {
            val neuronGrads = List(layer.size) {
                NeuronGradients(FloatArray(currentInputSize), 0f)
            }
            zeroGradients.add(LayerGradients(neuronGrads))
            currentInputSize = layer.size
        }
        return zeroGradients
    }

    private fun _addGradients(acc: List<LayerGradients>, sample: List<LayerGradients>): List<LayerGradients> {
        for (i in acc.indices) {
            for (j in acc[i].neuronGradients.indices) {
                for (k in acc[i].neuronGradients[j].weightGradients.indices) {
                    acc[i].neuronGradients[j].weightGradients[k] += sample[i].neuronGradients[j].weightGradients[k]
                }
                acc[i].neuronGradients[j].biasGradient += sample[i].neuronGradients[j].biasGradient
            }
        }
        return acc
    }

    private fun _averageGradients(acc: List<LayerGradients>, batchSize: Int): List<LayerGradients> {
        val batchSizeF = batchSize.toFloat()
        for (layerGrads in acc) {
            for (neuronGrads in layerGrads.neuronGradients) {
                for (k in neuronGrads.weightGradients.indices) {
                    neuronGrads.weightGradients[k] /= batchSizeF
                }
                neuronGrads.biasGradient /= batchSizeF
            }
        }
        return acc
    }
}