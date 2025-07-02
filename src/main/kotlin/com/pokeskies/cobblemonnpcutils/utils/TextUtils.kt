package com.pokeskies.cobblemonnpcutils.utils

import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import net.minecraft.network.chat.Component

object TextUtils {
    fun toNative(text: String): Component {
        return CobblemonNPCUtils.INSTANCE.adventure.toNative(CobblemonNPCUtils.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return CobblemonNPCUtils.MINI_MESSAGE.deserialize(text)
    }
}
