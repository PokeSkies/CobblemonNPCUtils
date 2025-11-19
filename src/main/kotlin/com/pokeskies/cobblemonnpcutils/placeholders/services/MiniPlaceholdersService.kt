package com.pokeskies.cobblemonnpcutils.placeholders.services

import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.placeholders.IPlaceholderService
import com.pokeskies.cobblemonnpcutils.placeholders.PlayerPlaceholder
import com.pokeskies.cobblemonnpcutils.placeholders.ServerPlaceholder
import com.pokeskies.cobblemonnpcutils.utils.Utils
import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.MiniPlaceholders
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minecraft.server.level.ServerPlayer

class MiniPlaceholdersService : IPlaceholderService {
    private val builder = Expansion.builder(CobblemonNPCUtils.MOD_ID)
    private val miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder().build())
        .build()

    init {
        Utils.printInfo("MiniPlaceholders mod found! Enabling placeholder integration...")
    }

    override fun parsePlaceholders(text: String, player: ServerPlayer?): String {
        val resolvers = mutableListOf(MiniPlaceholders.getGlobalPlaceholders())
        player?.let { resolvers.add(MiniPlaceholders.getAudiencePlaceholders(it)) }
        val resolver = TagResolver.resolver(*resolvers.toTypedArray())

        return CobblemonNPCUtils.INSTANCE.adventure.toNative(
            miniMessage.deserialize(text, resolver)
        ).string
    }

    override fun registerPlayer(placeholder: PlayerPlaceholder) {
        builder.filter(ServerPlayer::class.java)
            .audiencePlaceholder(placeholder.id()) { audience, queue, _ ->
                val player = audience as ServerPlayer
                val arguments: MutableList<String> = mutableListOf()
                while (queue.peek() != null) {
                    arguments.add(queue.pop().toString())
                }
                return@audiencePlaceholder Tag.preProcessParsed(placeholder.handle(player, arguments).string)
            }
    }

    override fun registerServer(placeholder: ServerPlaceholder) {
        builder.globalPlaceholder(placeholder.id()) { queue, ctx ->
            val arguments: MutableList<String> = mutableListOf()
            while (queue.peek() != null) {
                arguments.add(queue.pop().toString())
            }
            return@globalPlaceholder Tag.preProcessParsed(placeholder.handle(arguments).string)
        }.audiencePlaceholder(placeholder.id()) { audience, queue, ctx ->
            if (audience !is ServerPlayer) return@audiencePlaceholder Tag.inserting(Component.empty())
            val arguments: MutableList<String> = mutableListOf()
            while (queue.peek() != null) {
                arguments.add(queue.pop().toString())
            }
            return@audiencePlaceholder Tag.preProcessParsed(placeholder.handle(arguments).string)
        }
    }

    override fun finalizeRegister() {
        builder.build().register()
    }
}
