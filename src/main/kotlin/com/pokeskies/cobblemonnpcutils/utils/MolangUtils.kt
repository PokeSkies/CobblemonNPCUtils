package com.pokeskies.cobblemonnpcutils.utils

import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions
import com.cobblemon.mod.common.util.getIntOrNull
import com.cobblemon.mod.common.util.getStringOrNull
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.api.CobblemonNPCUtilsAPI
import com.pokeskies.cobblemonnpcutils.economy.EconomyType
import net.minecraft.server.level.ServerPlayer
import java.util.function.Function

object MolangUtils {
    fun setupMolang() {
        setupPlayerExtensions()
    }

    private fun setupPlayerExtensions() {
        MoLangFunctions.playerFunctions.add { player ->
            val map = hashMapOf<String, Function<MoParams, Any>>()
            // Item Functions!
            map["give_utils_item"] = Function { params -> // q.player.give_utils_item("<ITEM_DEF_ID>"[, <AMOUNT>])
                return@Function DoubleValue(if (CobblemonNPCUtilsAPI.giveItem(player, params.getString(0), params.getIntOrNull(1))) 1.0 else 0.0)
            }
            map["has_utils_item"] = Function { params -> // q.player.has_utils_item("<ITEM_DEF_ID>"[, <AMOUNT>])
                return@Function DoubleValue(if (CobblemonNPCUtilsAPI.hasItem(player, params.getString(0), params.getIntOrNull(1))) 1.0 else 0.0)
            }
            map["count_utils_item"] = Function { params -> // q.player.check_utils_item("<ITEM_DEF_ID>")
                return@Function DoubleValue(CobblemonNPCUtilsAPI.countItem(player, params.getString(0)))
            }
            map["take_utils_item"] = Function { params -> // q.player.take_utils_item("<ITEM_DEF_ID>"[, <AMOUNT>])
                return@Function DoubleValue(if (CobblemonNPCUtilsAPI.takeItem(player, params.getString(0), params.getIntOrNull(1))) 1.0 else 0.0)
            }

            // Teleport Functions!
            map["teleport_utils_location"] = Function { params -> // q.player.teleport_utils_location("<LOCATION_DEF_ID>")
                return@Function DoubleValue(if (CobblemonNPCUtilsAPI.teleportToLocation(player, params.getString(0))) 1.0 else 0.0)
            }

            // Economy Functions!
            map["deposit_economy"] = Function { params -> // q.player.give_economy("<PROVIDER>", <AMOUNT>[, "<CURRENCY:ID>"])
                val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                val amount = params.getDouble(1)
                val currency = params.getStringOrNull(2)

                val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                return@Function DoubleValue(if (service.deposit(player as ServerPlayer, amount, currency ?: "")) 1.0 else 0.0)
            }
            map["withdraw_economy"] = Function { params -> // q.player.withdraw_economy("<PROVIDER>", <AMOUNT>[, "<CURRENCY:ID>"])
                val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                val amount = params.getDouble(1)
                val currency = params.getStringOrNull(2)

                val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                return@Function DoubleValue(if (service.withdraw(player as ServerPlayer, amount, currency ?: "")) 1.0 else 0.0)
            }
            map["has_economy"] = Function { params -> // q.player.has_economy("<PROVIDER>", <AMOUNT>[, "<CURRENCY:ID>"])
                val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                val amount = params.getDouble(1)
                val currency = params.getStringOrNull(2)

                val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                return@Function DoubleValue(if (service.balance(player as ServerPlayer, currency ?: "") >= amount) 1.0 else 0.0)
            }
            map["balance_economy"] = Function { params -> // q.player.balance_economy("<PROVIDER>"[, "<CURRENCY:ID>"])
                val provider = EconomyType.valueOfAnyCase(params.getString(0)) ?: return@Function DoubleValue(0.0)
                val currency = params.getStringOrNull(1)

                val service = CobblemonNPCUtils.INSTANCE.getEconomyService(provider) ?: return@Function DoubleValue(0.0)

                return@Function DoubleValue(service.balance(player as ServerPlayer, currency ?: ""))
            }

            return@add map
        }
    }
}
