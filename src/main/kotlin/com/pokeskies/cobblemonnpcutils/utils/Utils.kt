package com.pokeskies.cobblemonnpcutils.utils

import com.google.gson.*
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.config.ConfigManager
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import java.lang.reflect.Type

object Utils {
    // Useful logging functions
    fun printDebug(message: String?, bypassCheck: Boolean = false) {
        if (bypassCheck || ConfigManager.CONFIG.debug)
            CobblemonNPCUtils.LOGGER.info("[${CobblemonNPCUtils.MOD_NAME}] DEBUG: $message")
    }

    fun printError(message: String?) {
        CobblemonNPCUtils.LOGGER.error("[${CobblemonNPCUtils.MOD_NAME}] ERROR: $message")
    }

    fun printInfo(message: String?) {
        CobblemonNPCUtils.LOGGER.info("[${CobblemonNPCUtils.MOD_NAME}] $message")
    }

    fun getLevel(name: String): ServerLevel? {
        val id = ResourceLocation.tryParse(name) ?: return null
        return CobblemonNPCUtils.INSTANCE.server
            .levelKeys()
            .firstOrNull { it.location() == id }
            ?.let { CobblemonNPCUtils.INSTANCE.server.getLevel(it) }
    }

    // Formats a time in seconds to the format "xd yh zm zs", but truncates unncessary parts
    fun getFormattedTime(time: Long): String {
        if (time <= 0) return "0s"
        val timeFormatted: MutableList<String> = ArrayList()
        val days = time / 86400
        val hours = time % 86400 / 3600
        val minutes = time % 86400 % 3600 / 60
        val seconds = time % 86400 % 3600 % 60
        if (days > 0) {
            timeFormatted.add(days.toString() + "d")
        }
        if (hours > 0) {
            timeFormatted.add(hours.toString() + "h")
        }
        if (minutes > 0) {
            timeFormatted.add(minutes.toString() + "m")
        }
        if (seconds > 0) {
            timeFormatted.add(seconds.toString() + "s")
        }
        return java.lang.String.join(" ", timeFormatted)
    }


    // Useful GSON seralizers for Minecraft Codecs. Thank you to Patbox for these
    data class RegistrySerializer<T>(val registry: Registry<T>) : JsonSerializer<T>, JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T? {
            var parsed = if (json.isJsonPrimitive) registry.get(ResourceLocation.tryParse(json.asString)) else null
            if (parsed == null)
                printError("There was an error while deserializing a Registry Type: $registry")
            return parsed
        }
        override fun serialize(src: T, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(registry.getId(src).toString())
        }
    }

    data class CodecSerializer<T>(val codec: Codec<T>) : JsonSerializer<T>, JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): T? {
            return try {
                codec.decode(JsonOps.INSTANCE, json).orThrow.first
            } catch (e: Throwable) {
                printError("There was an error while deserializing a Codec: $codec")
                null
            }
        }

        override fun serialize(src: T?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return try {
                if (src != null)
                    codec.encodeStart(JsonOps.INSTANCE, src).orThrow
                else
                    JsonNull.INSTANCE
            } catch (e: Throwable) {
                printError("There was an error while serializing a Codec: $codec")
                JsonNull.INSTANCE
            }
        }
    }
}
