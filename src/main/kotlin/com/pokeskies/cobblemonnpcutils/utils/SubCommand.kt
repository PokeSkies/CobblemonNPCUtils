package com.pokeskies.cobblemonnpcutils.utils

import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack

interface SubCommand {
    fun build(): LiteralCommandNode<CommandSourceStack>
}
