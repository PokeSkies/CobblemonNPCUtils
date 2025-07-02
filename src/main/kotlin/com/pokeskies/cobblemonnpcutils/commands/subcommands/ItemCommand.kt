package com.pokeskies.cobblemonnpcutils.commands.subcommands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.api.CobblemonNPCUtilsAPI
import com.pokeskies.cobblemonnpcutils.config.ConfigManager
import com.pokeskies.cobblemonnpcutils.config.ItemDefinition
import com.pokeskies.cobblemonnpcutils.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider

class ItemCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("item")
            .requires(Permissions.require("${CobblemonNPCUtils.MOD_ID}.command.item", 2))
            .then(Commands.literal("give")
                .then(Commands.argument("id", StringArgumentType.string())
                    .suggests { context, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.ITEM_DEFINITIONS.keys, builder)
                    }
                    .executes { ctx ->
                        give(ctx, StringArgumentType.getString(ctx, "id"))
                    }
                )
            )
            .then(Commands.literal("count")
                .then(Commands.argument("id", StringArgumentType.string())
                    .suggests { context, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.ITEM_DEFINITIONS.keys, builder)
                    }
                    .executes { ctx ->
                        count(ctx, StringArgumentType.getString(ctx, "id"))
                    }
                )
            )
            .then(Commands.literal("take")
                .then(Commands.argument("id", StringArgumentType.string())
                    .suggests { context, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.ITEM_DEFINITIONS.keys, builder)
                    }
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { ctx ->
                            take(ctx, StringArgumentType.getString(ctx, "id"), IntegerArgumentType.getInteger(ctx, "amount"))
                        }
                    )
                    .executes { ctx ->
                        take(ctx, StringArgumentType.getString(ctx, "id"))
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
                        SharedSuggestionProvider.suggest(ConfigManager.ITEM_DEFINITIONS.keys, builder)
                    }
                    .executes { ctx ->
                        delete(ctx, StringArgumentType.getString(ctx, "id"))
                    }
                )
            )
            .build()
    }

    companion object {
        fun give(
            ctx: CommandContext<CommandSourceStack>,
            itemId: String,
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            val item = CobblemonNPCUtilsAPI.getItemDefinition(itemId) ?: run {
                ctx.source.sendMessage(Component.text("Item Definition for ID '$itemId' was not found.", NamedTextColor.RED))
                return 0
            }

            if (!CobblemonNPCUtilsAPI.giveItem(player, item)) {
                ctx.source.sendMessage(Component.text("Failed to give item '$itemId' to player '${player.gameProfile.name}'.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Successfully gave item '$itemId' to player '${player.gameProfile.name}'.", NamedTextColor.GREEN))
            return 1
        }

        fun count(
            ctx: CommandContext<CommandSourceStack>,
            itemId: String,
            ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            val item = ConfigManager.ITEM_DEFINITIONS[itemId] ?: run {
                ctx.source.sendMessage(Component.text("Item Definition for ID '$itemId' was not found.", NamedTextColor.RED))
                return 0
            }

            val count = CobblemonNPCUtilsAPI.countItem(player, item)

            if (count <= 0) {
                ctx.source.sendMessage(Component.text("Player '${player.gameProfile.name}' does not have the item '$itemId'.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Player '${player.gameProfile.name}' has ${count}x of the item '$itemId'.", NamedTextColor.GREEN))
            return 1
        }

        fun take(
            ctx: CommandContext<CommandSourceStack>,
            itemId: String,
            amount: Int = 1
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            if (CobblemonNPCUtilsAPI.takeItem(player, itemId, amount)) {
                ctx.source.sendMessage(Component.text("Successfully took $amount of item '$itemId' from player '${player.gameProfile.name}'.", NamedTextColor.GREEN))
            } else {
                ctx.source.sendMessage(Component.text("Failed to take $amount of item '$itemId' from player '${player.gameProfile.name}'.", NamedTextColor.RED))
                return 0
            }

            return 1
        }

        fun create(
            ctx: CommandContext<CommandSourceStack>,
            itemId: String
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            if (ConfigManager.ITEM_DEFINITIONS.containsKey(itemId)) {
                ctx.source.sendMessage(Component.text("Item Definition for ID '$itemId' already exists.", NamedTextColor.RED))
                return 0
            }

            val itemStack = player.mainHandItem ?: run {
                ctx.source.sendMessage(Component.text("You must be holding an item to create an Item Definition.", NamedTextColor.RED))
                return 0
            }

            ConfigManager.ITEM_DEFINITIONS[itemId] = CobblemonNPCUtilsAPI.asItemDefinition(itemStack)

            if (!ConfigManager.saveFile("items.json", ConfigManager.ITEM_DEFINITIONS)) {
                ctx.source.sendMessage(Component.text("Failed to save changes to items.json.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Successfully created Item Definition for ID '$itemId'.", NamedTextColor.GREEN))
            return 1
        }


        fun delete(
            ctx: CommandContext<CommandSourceStack>,
            itemId: String
        ): Int {
            val player = ctx.source.player ?: run {
                ctx.source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED))
                return 0
            }

            if (!ConfigManager.ITEM_DEFINITIONS.containsKey(itemId)) {
                ctx.source.sendMessage(Component.text("Item Definition for ID '$itemId' does not exist.", NamedTextColor.RED))
                return 0
            }

            ConfigManager.ITEM_DEFINITIONS.remove(itemId)

            if (!ConfigManager.saveFile("items.json", ConfigManager.ITEM_DEFINITIONS)) {
                ctx.source.sendMessage(Component.text("Failed to save changes to items.json.", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Successfully deleted Item Definition for ID '$itemId'.", NamedTextColor.GREEN))
            return 1
        }
    }
}
