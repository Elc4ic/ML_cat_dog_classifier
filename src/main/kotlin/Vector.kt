package org.example

object Vector {
    fun dot(v1: FloatArray, v2: FloatArray): Float {
        return v1.zip(v2) { f1, f2 -> f1 * f2 }.sum()
    }

}