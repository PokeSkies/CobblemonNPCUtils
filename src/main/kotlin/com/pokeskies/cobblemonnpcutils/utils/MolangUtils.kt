package com.pokeskies.cobblemonnpcutils.utils

import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.api.molang.function.PlayerMoLangFunctions
import com.cobblemon.mod.common.util.getBooleanOrNull
import com.cobblemon.mod.common.util.getIntOrNull
import com.cobblemon.mod.common.util.getStringOrNull
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.api.CobblemonNPCUtilsAPI
import com.pokeskies.cobblemonnpcutils.economy.EconomyType
import com.pokeskies.cobblemonnpcutils.placeholders.PlaceholderManager
import com.pokeskies.cobblemonnpcutils.placeholders.PlaceholderMods
import net.minecraft.server.level.ServerPlayer
import java.util.function.Function

object MolangUtils {
    fun setupMolang() {
        setupPlayerExtensions()
    }

    private fun setupPlayerExtensions() {
        PlayerMoLangFunctions.custom.add { player ->
            mapOf(
                // Item Functions!
                "give_utils_item" to { params: MoParams -> // q.player.give_utils_item("<ITEM_DEF_ID>"[, <AMOUNT>])
                    DoubleValue(if (CobblemonNPCUtilsAPI.giveDefinedItem(player, params.getString(0), params.getIntOrNull(1))) 1.0 else 0.0)
                },
                "has_utils_item" to { params: MoParams -> // q.player.has_utils_item("<ITEM_DEF_ID>"[, <AMOUNT>])
                    DoubleValue(if (CobblemonNPCUtilsAPI.hasDefinedItem(player, params.getString(0), params.getIntOrNull(1))) 1.0 else 0.0)
                },
                "count_utils_item" to { params -> // q.player.check_utils_item("<ITEM_DEF_ID>")
                    DoubleValue(CobblemonNPCUtilsAPI.countDefinedItem(player, params.getString(0)))
                },
                "take_utils_item" to { params -> // q.player.take_utils_item("<ITEM_DEF_ID>"[, <AMOUNT>])
                    DoubleValue(if (CobblemonNPCUtilsAPI.takeDefinedItem(player, params.getString(0), params.getIntOrNull(1))) 1.0 else 0.0)
                },

                "give_item" to { params -> // q.player.give_item("<ITEM_DEF>"[, <AMOUNT>, <SHOULD_DROP>])
                    DoubleValue(if (GenericItemUtils.giveGenericItem(player, params.getString(0), params.getIntOrNull(1) ?: 1, params.getBooleanOrNull(2) ?: false)) 1.0 else 0.0)
                },
                "has_item" to { params -> // q.player.has_item("<ITEM_DEF>"[, <AMOUNT>, <STRICT>])
                    DoubleValue(if (GenericItemUtils.hasGenericItem(player, params.getString(0), params.getIntOrNull(1) ?: 1, params.getBooleanOrNull(2) ?: false)) 1.0 else 0.0)
                },
                "count_item" to { params -> // q.player.count_item("<ITEM_DEF>"[, <STRICT>])
                    DoubleValue(GenericItemUtils.countGenericItem(player, params.getString(0), params.getBooleanOrNull(1) ?: false))
                },
                "take_item" to { params -> // q.player.has_item("<ITEM_DEF>"[, <AMOUNT>, <STRICT>])
                    DoubleValue(if (GenericItemUtils.takeGenericItem(player, params.getString(0), params.getIntOrNull(1) ?: 1, params.getBooleanOrNull(2) ?: false)) 1.0 else 0.0)
                },

                // Teleport Functions!
                "teleport_utils_location" to { params -> // q.player.teleport_utils_location("<LOCATION_DEF_ID>")
                    DoubleValue(if (CobblemonNPCUtilsAPI.teleportToLocation(player, params.getString(0))) 1.0 else 0.0)
                },

                // Economy Functions!
                "deposit_economy" to Function@{ params -> // q.player.deposit_economy("<PROVIDER>", <AMOUNT>[, "<CURRENCY:ID>"])
                    val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                    val amount = params.getDouble(1)
                    val currency = params.getStringOrNull(2)

                    val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                    DoubleValue(if (service.deposit(player as ServerPlayer, amount, currency ?: "")) 1.0 else 0.0)
                },
                "withdraw_economy" to Function@{ params -> // q.player.withdraw_economy("<PROVIDER>", <AMOUNT>[, "<CURRENCY:ID>"])
                    val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                    val amount = params.getDouble(1)
                    val currency = params.getStringOrNull(2)

                    val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                    DoubleValue(if (service.withdraw(player as ServerPlayer, amount, currency ?: "")) 1.0 else 0.0)
                },
                "has_economy" to Function@{ params -> // q.player.has_economy("<PROVIDER>", <AMOUNT>[, "<CURRENCY:ID>"])
                    val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                    val amount = params.getDouble(1)
                    val currency = params.getStringOrNull(2)

                    val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                    return@Function DoubleValue(if (service.balance(player as ServerPlayer, currency ?: "") >= amount) 1.0 else 0.0)
                },
                "balance_economy" to Function@{ params -> // q.player.balance_economy("<PROVIDER>"[, "<CURRENCY:ID>"])
                    val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                    val currency = params.getStringOrNull(1)

                    val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                    DoubleValue(service.balance(player as ServerPlayer, currency ?: ""))
                },

                // Placeholders
                "parse_placeholders" to { params -> // q.player.parse_placeholders("<STRING>"[, "<SERVICE>"])
                    // Gather an list of services to use, if none are specified, use all in PlaceholderMods
                    val mods = params.getStringOrNull(1)
                        ?.split(",")
                        ?.asSequence()
                        ?.map(String::trim)
                        ?.filter(String::isNotEmpty)
                        ?.mapNotNull(PlaceholderMods::valueOfAnyCase)
                        ?.toList()
                        ?.takeIf { it.isNotEmpty() }
                        ?: PlaceholderMods.entries

                    val providers = mods.map(PlaceholderManager::getServiceForType)
                    var value = params.getString(0)
                    providers.forEach { value = it.parsePlaceholders(value, player as ServerPlayer) }
                    StringValue(value)
                }
            )
        }
    }
}
