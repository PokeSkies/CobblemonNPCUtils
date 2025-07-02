package com.pokeskies.cobblemonnpcutils.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.commands.subcommands.DebugCommand
import com.pokeskies.cobblemonnpcutils.commands.subcommands.ItemCommand
import com.pokeskies.cobblemonnpcutils.commands.subcommands.LocationCommand
import com.pokeskies.cobblemonnpcutils.commands.subcommands.ReloadCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class BaseCommand {
    private val aliases = listOf("cobblemonnpcutils", "npcutils")

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires(Permissions.require("${CobblemonNPCUtils.MOD_ID}.command.base", 2))
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            ReloadCommand().build(),
            DebugCommand().build(),
            ItemCommand().build(),
            LocationCommand().build(),
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }
}
