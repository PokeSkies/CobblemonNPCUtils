package com.pokeskies.cobblemonnpcutils.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.api.CobblemonNPCUtilsAPI
import com.pokeskies.cobblemonnpcutils.config.ConfigManager
import com.pokeskies.cobblemonnpcutils.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider

class LocationCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("location")
            .requires(Permissions.require("${CobblemonNPCUtils.MOD_ID}.command.location", 2))
            .then(Commands.literal("teleport")
                .then(Commands.argument("id", StringArgumentType.string())
                    .suggests { context, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.LOCATION_DEFINITIONS.keys, builder)
                    }
                    .executes { ctx ->
                        teleport(ctx, StringArgumentType.getString(ctx, "id"))
                    }
                )
            )
            .then(Commands.literal("create")
                .then(Commands.argument("id", StringArgumentType.string())
                    .executes { ctx ->
                        create(ctx, StringArgumentType.getString(ctx, "id"))
                    }
                )
            )
            .then(Commands.literal("delete")
                .then(Commands.argument("id", StringArgumentType.string())
                    .suggests { context, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.LOCATION_DEFINITIONS.keys, builder)
                    }
                    .executes { ctx ->
                        delete(ctx, StringArgumentType.getString(ctx, "id"))
                    }
                )
            )
            .build()
    }

    companion object {
        fun teleport(
            ctx: CommandContext<CommandSourceStack>,
            locationId: String,
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            val location = CobblemonNPCUtilsAPI.getLocationDefinition(locationId) ?: run {
                ctx.source.sendMessage(Component.text("Location definition '$locationId' does not exist.", NamedTextColor.RED))
                return 0
            }

            if (!CobblemonNPCUtilsAPI.teleportToLocation(player, location)) {
                ctx.source.sendMessage(Component.text("Failed to teleport to location '$locationId'.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Teleported to location '$locationId'.", NamedTextColor.GREEN))
            return 1
        }

        fun create(
            ctx: CommandContext<CommandSourceStack>,
            locationId: String
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            if (ConfigManager.LOCATION_DEFINITIONS.containsKey(locationId)) {
                ctx.source.sendMessage(Component.text("Location definition '$locationId' already exists.", NamedTextColor.RED))
                return 0
            }

            ConfigManager.LOCATION_DEFINITIONS[locationId] = CobblemonNPCUtilsAPI.asLocationDefinition(player)

            if (!ConfigManager.saveFile("locations.json", ConfigManager.LOCATION_DEFINITIONS)) {
                ctx.source.sendMessage(Component.text("Failed to save the new location definition.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Created new location definition '$locationId' based on your position and orientation.", NamedTextColor.GREEN))
            return 1
        }


        fun delete(
            ctx: CommandContext<CommandSourceStack>,
            locationId: String
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            if (!ConfigManager.LOCATION_DEFINITIONS.containsKey(locationId)) {
                ctx.source.sendMessage(Component.text("Location definition '$locationId' does not exist.", NamedTextColor.RED))
                return 0
            }

            ConfigManager.LOCATION_DEFINITIONS.remove(locationId)

            if (!ConfigManager.saveFile("locations.json", ConfigManager.LOCATION_DEFINITIONS)) {
                ctx.source.sendMessage(Component.text("Failed to save the deletion of the location definition.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Deleted location definition '$locationId'.", NamedTextColor.GREEN))
            return 1
        }
    }
}
