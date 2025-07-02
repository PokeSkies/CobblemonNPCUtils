package com.pokeskies.cobblemonnpcutils.config

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.utils.FlexibleListAdaptorFactory
import com.pokeskies.cobblemonnpcutils.utils.TextUtils
import com.pokeskies.cobblemonnpcutils.utils.Utils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import kotlin.jvm.optionals.getOrNull

class ItemDefinition(
    val item: String = "",
    @SerializedName("slots", alternate = ["slot"])
    val amount: Int = 1,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    @SerializedName("custom_model_data")
    val customModelData: Int? = null,
    @SerializedName("components", alternate = ["nbt"])
    val components: CompoundTag? = null,
) {
    fun createItemStack(amountOverride: Int? = null): ItemStack {
        if (item.isEmpty()) {
            Utils.printError("Error while getting item '$item'! It was found to be empty, defaulting to air...")
            return ItemStack(Items.AIR, amount)
        }
        val rl = ResourceLocation.tryParse(item) ?: run {
            Utils.printError("Error while parsing item '$item'! Item provided could not be parsed into the format 'MOD_ID:ITEM_ID', defaulting to air...")
            return ItemStack(Items.AIR, amount)
        }
        val parsedItem = BuiltInRegistries.ITEM.getOptional(rl).getOrNull() ?: run {
            Utils.printError("Error while getting item '$item'! Item ID ('$rl') not found in registry, defaulting to air...")
            return ItemStack(Items.AIR, amount)
        }

        val itemStack = ItemStack(parsedItem, amountOverride ?: amount)

        if (components != null) {
            DataComponentPatch.CODEC.decode(CobblemonNPCUtils.INSTANCE.nbtOpts, components).result().ifPresent { result ->
                itemStack.applyComponents(result.first)
            }
        }

        val dataComponents = DataComponentPatch.builder()

        if (customModelData != null) {
            dataComponents.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(customModelData))
        }

        if (name != null)
            dataComponents.set(DataComponents.ITEM_NAME, TextUtils.toNative(name))

        if (lore.isNotEmpty()) {
            val parsedLore: MutableList<String> = mutableListOf()
            for (line in lore.stream().map { it }.toList()) {
                if (line.contains("\n")) {
                    line.split("\n").forEach { parsedLore.add(it) }
                } else {
                    parsedLore.add(line)
                }
            }
            dataComponents.set(
                DataComponents.LORE, ItemLore(
                parsedLore.stream().map { line ->
                    Component.empty().withStyle { it.withItalic(false) }
                        .append(TextUtils.toNative(line))
                }.toList() as List<Component>
            )
            )
        }

        itemStack.applyComponents(dataComponents.build())

        return itemStack
    }

    // Checks if the item matches the comparison item stack, ignoring amount
    fun matches(comparison: ItemStack, prebuiltItemStack: ItemStack? = null): Boolean {
        val itemStack = prebuiltItemStack ?: createItemStack()

        if (itemStack.item != comparison.item) return false
        if (itemStack.components != comparison.components) return false

        return true
    }

    override fun toString(): String {
        return "ItemDefinition(item='$item', amount=$amount, name=$name, lore=$lore, customModelData=$customModelData, components=$components)"
    }
}
