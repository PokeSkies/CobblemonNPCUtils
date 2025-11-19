package com.pokeskies.cobblemonnpcutils.placeholders

import net.minecraft.server.level.ServerPlayer

interface IPlaceholderService {
    fun parsePlaceholders(text: String, player: ServerPlayer?): String
    fun registerPlayer(placeholder: PlayerPlaceholder)
    fun registerServer(placeholder: ServerPlaceholder)
    fun finalizeRegister()
}
