package com.pokeskies.cobblemonnpcutils.placeholders

import net.kyori.adventure.text.Component

class GenericResult private constructor(
    val string: String,
    val isSuccessful: Boolean = true
) {
    companion object {
        fun valid(result: String): GenericResult {
            return GenericResult(result, true)
        }

        fun valid(result: Int): GenericResult {
            return valid(result.toString())
        }

        fun valid(result: Boolean): GenericResult {
            return valid(result.toString())
        }

        fun valid(result: Float): GenericResult {
            return valid(result.toString())
        }

        fun valid(result: Double): GenericResult {
            return valid(result.toString())
        }

        fun invalid(result: String): GenericResult {
            return GenericResult(result, false)
        }
    }

    fun asComponent(): Component {
        return Component.text(string)
    }
}