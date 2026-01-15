package com.pokeskies.cobblemonnpcutils.utils

import com.mojang.brigadier.StringReader
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import net.minecraft.commands.arguments.item.ItemInput
import net.minecraft.commands.arguments.item.ItemParser
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object GenericItemUtils {
    private val itemParser = ItemParser(CobblemonNPCUtils.INSTANCE.server.registryAccess())

    fun hasGenericItem(player: Player, itemDef: String, amount: Int, strict: Boolean = false): Boolean {
        return try {
            val parsed = itemParser.parse(StringReader(itemDef))
            val result = ItemInput(parsed.item, parsed.components)
            val stack = result.createItemStack(1, false)

            val slots: MutableMap<Int, ItemStack> = mutableMapOf()
            for ((i, item) in player.inventory.items.withIndex()) {
                if (item.isEmpty) continue
                if (stacksMatch(item, stack, strict)) {
                    slots[i] = item
                }
            }

            if (slots.isEmpty()) {
                return false
            }

            slots.values.sumOf { it.count } >= amount
        } catch (e: Exception) {
            Utils.printError("Failed to parse item definition '$itemDef' for molang function: ${e.printStackTrace()}")
            false
        }
    }

    fun countGenericItem(player: Player, itemDef: String, strict: Boolean = false): Int {
        return try {
            val parsed = itemParser.parse(StringReader(itemDef))
            val result = ItemInput(parsed.item, parsed.components)
            val stack = result.createItemStack(1, false)

            val slots: MutableMap<Int, ItemStack> = mutableMapOf()
            for ((i, item) in player.inventory.items.withIndex()) {
                if (item.isEmpty) continue
                if (stacksMatch(item, stack, strict)) {
                    slots[i] = item
                }
            }

            if (slots.isEmpty()) {
                return 0
            }

            slots.values.sumOf { it.count }
        } catch (e: Exception) {
            Utils.printError("Failed to parse item definition '$itemDef' for molang function: ${e.printStackTrace()}")
            -1
        }
    }

    fun takeGenericItem(player: Player, itemDef: String, amount: Int, strict: Boolean = false): Boolean {
        return try {
            val parsed = itemParser.parse(StringReader(itemDef))
            val result = ItemInput(parsed.item, parsed.components)
            val stack = result.createItemStack(1, false)

            val slots: MutableMap<Int, ItemStack> = mutableMapOf()
            for ((i, item) in player.inventory.items.withIndex()) {
                if (item.isEmpty) continue
                if (stacksMatch(item, stack, strict)) {
                    slots[i] = item
                }
            }

            if (slots.isEmpty()) {
                return false
            }
            if (slots.values.sumOf { it.count } >= amount) {
                var amountTaken = 0
                for ((slot, stack) in slots) {
                    if (amountTaken >= amount) break
                    if (stack.count > amount) {
                        player.inventory.setItem(slot, stack.split(stack.count - amount))
                        amountTaken += amount
                    } else {
                        player.inventory.setItem(slot, ItemStack.EMPTY)
                        amountTaken += stack.count
                    }
                }

                return amountTaken >= amount
            }

            return false
        } catch (e: Exception) {
            Utils.printError("Failed to parse item definition '$itemDef' for molang function: ${e.printStackTrace()}")
            false
        }
    }

    fun giveGenericItem(player: Player, itemDef: String, amount: Int, shouldDrop: Boolean = false): Boolean {
        return try {
            val parsed = itemParser.parse(StringReader(itemDef))
            val result = ItemInput(parsed.item, parsed.components)
            val stack = result.createItemStack(amount, false)

            if (shouldDrop) {
                player.inventory.placeItemBackInInventory(stack)
                return true
            }

            player.inventory.add(stack)
        } catch (e: Exception) {
            Utils.printError("Failed to parse item definition '$itemDef' for molang function: ${e.printStackTrace()}")
            false
        }
    }

    private fun stacksMatch(itemStack: ItemStack, comparison: ItemStack, strict: Boolean = false): Boolean {
        if (itemStack.item != comparison.item) return false
        if (strict && itemStack.components != comparison.components) return false

        return true
    }
}