package com.pokeskies.cobblemonnpcutils.api

import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.config.ConfigManager
import com.pokeskies.cobblemonnpcutils.config.ItemDefinition
import com.pokeskies.cobblemonnpcutils.config.LocationDefinition
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

object CobblemonNPCUtilsAPI {
    // Item Definition Stuff!
    fun giveItem(player: Player, itemId: String, amountOverride: Int? = null, shouldDrop: Boolean = false): Boolean {
        val def = getItemDefinition(itemId) ?: return false
        return giveItem(player, def, amountOverride, shouldDrop)
    }

    fun giveItem(player: Player, itemDef: ItemDefinition, amountOverride: Int? = null, shouldDrop: Boolean = false): Boolean {
        val stack = itemDef.createItemStack(amountOverride)
        if (shouldDrop) {
            player.inventory.placeItemBackInInventory(stack)
            return true
        }
        return player.addItem(stack)
    }

    // Has the specified item with the configured amount or override amount
    fun hasItem(player: Player, itemId: String, amountOverride: Int? = null): Boolean {
        val def = getItemDefinition(itemId) ?: return false
        return hasItem(player, def, amountOverride)
    }

    // Has the specified item with the configured amount or override amount
    fun hasItem(player: Player, itemDef: ItemDefinition, amountOverride: Int? = null): Boolean {
        return countItem(player, itemDef) >= (amountOverride ?: itemDef.amount)
    }

    fun countItem(player: Player, itemId: String): Int {
        val def = getItemDefinition(itemId) ?: return 0
        return countItem(player, def)
    }

    fun countItem(player: Player, itemDef: ItemDefinition): Int {
        val stack = itemDef.createItemStack()
        var count = 0

        for (item in player.inventory.items) {
            if (item.isEmpty) continue
            if (itemDef.matches(item, stack)) {
                count += item.count
            }
        }

        return count
    }

    // Will take the specified item with the configured amount or override amount, but ONLY if the amount is reached
    fun takeItem(player: Player, itemId: String, amountOverride: Int? = null): Boolean {
        val def = getItemDefinition(itemId) ?: return false
        return takeItem(player, def, amountOverride)
    }

    // Will take the specified item with the configured amount or override amount, but ONLY if the amount is reached
    fun takeItem(player: Player, itemDef: ItemDefinition, amountOverride: Int? = null): Boolean {
        val stack = itemDef.createItemStack()
        val slots: MutableMap<Int, ItemStack> = mutableMapOf()

        for ((i, item) in player.inventory.items.withIndex()) {
            if (item.isEmpty) continue
            if (itemDef.matches(item, stack)) {
                slots[i] = item
            }
        }

        if (slots.isEmpty()) {
            return false
        }
        val amountToTake = amountOverride ?: itemDef.amount
        if (slots.values.sumOf { it.count } >= amountToTake) {
            var amountTaken = 0
            for ((slot, stack) in slots) {
                if (amountTaken >= amountToTake) break
                if (stack.count > amountToTake) {
                    player.inventory.setItem(slot, stack.split(amountToTake))
                    amountTaken += amountToTake
                } else {
                    player.inventory.setItem(slot, ItemStack.EMPTY)
                    amountTaken += stack.count
                }
            }

            return amountTaken >= amountToTake
        }


        return false
    }

    fun getItemDefinition(itemId: String): ItemDefinition? {
        return ConfigManager.ITEM_DEFINITIONS[itemId]
    }

    fun teleportToLocation(player: Player, locationId: String): Boolean {
        val locationDef = getLocationDefinition(locationId) ?: return false
        return teleportToLocation(player, locationDef)
    }

    fun teleportToLocation(player: Player, location: LocationDefinition): Boolean {
        return location.teleportPlayer(player)
    }

    fun getLocationDefinition(locationId: String): LocationDefinition? {
        return ConfigManager.LOCATION_DEFINITIONS[locationId]
    }

    fun asItemDefinition(itemStack: ItemStack): ItemDefinition {
        val item = BuiltInRegistries.ITEM.getKey(itemStack.item).toString()
        val amount = itemStack.count
        val tag = DataComponentPatch.CODEC.encodeStart(CobblemonNPCUtils.INSTANCE.nbtOpts, itemStack.componentsPatch).result().getOrNull()
        val components = tag as? CompoundTag

        return ItemDefinition(item, amount, components = components)
    }

    fun asLocationDefinition(player: ServerPlayer): LocationDefinition {
        return asLocationDefinition(
            player.serverLevel(),
            player.x,
            player.y,
            player.z,
            player.yRot,
            player.xRot
        )
    }

    fun asLocationDefinition(level: ServerLevel, x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f): LocationDefinition {
        return LocationDefinition(level.dimension().location().toString(), x, y, z, yaw, pitch)
    }
}
