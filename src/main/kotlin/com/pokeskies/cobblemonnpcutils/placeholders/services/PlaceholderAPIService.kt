package com.pokeskies.cobblemonnpcutils.placeholders.services

import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.placeholders.IPlaceholderService
import com.pokeskies.cobblemonnpcutils.placeholders.PlayerPlaceholder
import com.pokeskies.cobblemonnpcutils.placeholders.ServerPlaceholder
import com.pokeskies.cobblemonnpcutils.utils.Utils
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class PlaceholderAPIService : IPlaceholderService {
    init {
        Utils.printInfo("PlaceholderAPI mod found! Enabling placeholder integration...")
    }

    override fun parsePlaceholders(text: String, player: ServerPlayer?): String {
        return Placeholders.parseText(Component.literal(text),
            if (player != null)
                PlaceholderContext.of(player)
            else
                PlaceholderContext.of(CobblemonNPCUtils.INSTANCE.server)
        ).string
    }



    override fun registerPlayer(placeholder: PlayerPlaceholder) {
        Placeholders.register(ResourceLocation.fromNamespaceAndPath("cobblemon", placeholder.id())) { ctx, arg ->
            val player = ctx.player ?: return@register PlaceholderResult.invalid("NO PLAYER")
            val args = if (arg != null) parsePlaceholders(arg, player).split(":") else emptyList()
            val result = placeholder.handle(player, args)
            return@register if (result.isSuccessful) {
                PlaceholderResult.value(CobblemonNPCUtils.INSTANCE.adventure.toNative(result.asComponent()))
            } else {
                PlaceholderResult.invalid(result.string)
            }
        }
    }

    override fun registerServer(placeholder: ServerPlaceholder) {
        Placeholders.register(ResourceLocation.fromNamespaceAndPath("cobblemon", placeholder.id())) { ctx, arg ->
            val args = if (arg != null) parsePlaceholders(arg, ctx.player).split(":") else emptyList()
            val result = placeholder.handle(args.map { parsePlaceholders(it, null) })
            return@register if (result.isSuccessful) {
                PlaceholderResult.value(CobblemonNPCUtils.INSTANCE.adventure.toNative(result.asComponent()))
            } else {
                PlaceholderResult.invalid(result.string)
            }
        }
    }

    override fun finalizeRegister() {

    }
}
