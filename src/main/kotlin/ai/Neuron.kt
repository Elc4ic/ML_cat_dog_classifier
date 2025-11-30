package org.example.ai

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

enum class Function {
    RELU,
    RELULOSS,
    TANH,
    SIGMOID
}

fun String.toFunction(): Function {
    return when (this) {
        "RELU" -> Function.RELU
        "RELULOSS" -> Function.RELULOSS
        "TANH" -> Function.TANH
        "SIGMOID" -> Function.SIGMOID
        else -> Function.RELU
    }
}

class NeuronFunction(
    f: Function
) {
    private fun kaiming(size: Int) = sqrt(2f / size.toFloat())
    private fun xavier(size: Int) = sqrt(1f / size)
    private fun relu(x: Float) = max(0f, x)
    private fun reluLoss(x: Float) = if (x > 0) x else x * 0.01f
    private fun sigmoid(x: Float) = 1f / (1f + exp(-x))
    private fun tanh(x: Float) = kotlin.math.tanh(x)
    private fun reluDeriv(x: Float) = if (x > 0f) 1f else 0f
    private fun sigmoidDeriv(x: Float): Float {
        val s = sigmoid(x)
        return s * (1f - s)
    }

    private fun tanhDeriv(x: Float) = 1f - tanh(x).pow(2)

    val outputFun = when (f) {
        Function.RELU -> ::relu
        Function.SIGMOID -> ::sigmoid
        Function.TANH -> ::tanh
        Function.RELULOSS -> ::reluLoss
    }

    val activationDeriv = when (f) {
        Function.RELU -> ::reluDeriv
        Function.RELULOSS -> ::reluDeriv
        Function.SIGMOID -> ::sigmoidDeriv
        Function.TANH -> ::tanhDeriv
    }

    val weightInitFun = when (f) {
        Function.RELU -> ::kaiming
        Function.RELULOSS -> ::kaiming
        Function.SIGMOID -> ::xavier
        Function.TANH -> ::xavier
    }
}

data class NeuronGradients(
    val weightGradients: FloatArray,
    var biasGradient: Float
)

class Neuron(val calculator: NeuronFunction) {
    lateinit var weight: FloatArray
    var bias = 0f
    private var lastOutput = 0f

    fun initWeights(inputSize: Int) {
        val scale = calculator.weightInitFun(inputSize)
        weight = FloatArray(inputSize) { (Random.nextFloat() * 2 - 1) * scale }
    }

    fun calc(input: FloatArray): Float {
        lastOutput = Vector.dot(input, weight) + bias
        return calculator.outputFun(lastOutput)
    }

    fun calculateGradients(error: Float, input: FloatArray): Pair<Float, NeuronGradients> {
        val d = calculator.activationDeriv(lastOutput) * error

        val weightGradients = FloatArray(input.size) { i -> d * input[i] }
        val biasGradient = d

        val gradients = NeuronGradients(weightGradients, biasGradient)

        return Pair(d, gradients)
    }


    fun applyGradients(gradients: NeuronGradients, lr: Float) {
        for (i in weight.indices) {
            weight[i] -= lr * gradients.weightGradients[i]
        }
        bias -= lr * gradients.biasGradient
    }
}
