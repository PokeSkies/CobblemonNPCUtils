package com.pokeskies.cobblemonnpcutils.placeholders

import net.fabricmc.loader.api.FabricLoader

enum class PlaceholderMods(val modId: String, val identifiers: List<String>) {
    IMPACTOR("impactor", listOf("impactor", "impactorapi")),
    PLACEHOLDERAPI("placeholder-api", listOf("papi", "placeholderapi", "textplaceholderapi")),
    MINIPLACEHOLDERS("miniplaceholders", listOf("miniplaceholders", "mini"));

    fun isModPresent() : Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    companion object {
        fun valueOfAnyCase(name: String): PlaceholderMods? {
            for (type in entries) {
                if (type.identifiers.any { name.equals(it, true) }) return type
            }
            return null
        }
    }
}
