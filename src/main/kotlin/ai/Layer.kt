package org.example.ai

data class LayerGradients(val neuronGradients: List<NeuronGradients>)
class Layer(val size: Int, val name: String,val f: Function) {
    val neurons = Array(size) { Neuron(NeuronFunction(f)) }

    fun initWeights(inputSize: Int) {
        for (neuron in neurons) neuron.initWeights(inputSize)
    }

    fun calc(input: FloatArray): FloatArray {
        return FloatArray(neurons.size) { neurons[it].calc(input) }
    }

    fun backward(error: FloatArray, input: FloatArray): Pair<FloatArray, LayerGradients> {
        val newError = FloatArray(input.size) { 0f }
        val layerGradients = mutableListOf<NeuronGradients>()

        for ((i, neuron) in neurons.withIndex()) {
            val (delta, gradients) = neuron.calculateGradients(error[i], input)
            layerGradients.add(gradients)
            for (j in input.indices) {
                newError[j] += neuron.weight[j] * delta
            }
        }
        return Pair(newError, LayerGradients(layerGradients))
    }

    fun applyGradients(gradients: LayerGradients, lr: Float) {
        for (i in neurons.indices) {
            neurons[i].applyGradients(gradients.neuronGradients[i], lr)
        }
    }

}