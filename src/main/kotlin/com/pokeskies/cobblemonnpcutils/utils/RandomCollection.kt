package com.pokeskies.cobblemonnpcutils.utils

import java.util.*
import kotlin.random.Random

class RandomCollection<E> @JvmOverloads constructor(random: Random = Random) {
    private val map: NavigableMap<Double, E> = TreeMap()
    private val random: Random
    private var total = 0.0

    init {
        this.random = random
    }

    fun add(weight: Double, result: E): RandomCollection<E> {
        if (weight <= 0) return this
        total += weight
        map[total] = result
        return this
    }

    operator fun next(): E {
        val value: Double = random.nextDouble() * total
        return map.higherEntry(value).value
    }

    fun size(): Int {
        return map.size
    }
}
