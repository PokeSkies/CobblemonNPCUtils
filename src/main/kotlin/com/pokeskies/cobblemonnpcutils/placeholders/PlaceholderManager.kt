package com.pokeskies.cobblemonnpcutils.placeholders

import com.pokeskies.cobblemonnpcutils.placeholders.services.ImpactorPlaceholderService
import com.pokeskies.cobblemonnpcutils.placeholders.services.MiniPlaceholdersService
import com.pokeskies.cobblemonnpcutils.placeholders.services.PlaceholderAPIService
import net.minecraft.server.level.ServerPlayer

object PlaceholderManager {
    private val services: MutableList<IPlaceholderService> = mutableListOf()

    fun init() {
        for (service in PlaceholderMods.entries) {
            if (service.isModPresent()) {
                services.add(getServiceForType(service))
            }
        }
        registerPlaceholders()
    }

    private fun registerPlaceholders() {
        // SERVER PLACEHOLDERS
//        Stream.of(

//        ).forEach { placeholder -> services.forEach { it.registerServer(placeholder) } }

        // PLAYER PLACEHOLDERS
//        Stream.of(

//        ).forEach { placeholder -> services.forEach { it.registerPlayer(placeholder) } }

        services.forEach { it.finalizeRegister() }
    }

    fun parse(player: ServerPlayer, text: String): String {
        var returnValue = text
        for (service in services) {
            returnValue = service.parsePlaceholders(returnValue, player)
        }
        return returnValue
    }

    fun getServiceForType(placeholderMod: PlaceholderMods): IPlaceholderService {
        return when (placeholderMod) {
            PlaceholderMods.IMPACTOR -> ImpactorPlaceholderService()
            PlaceholderMods.PLACEHOLDERAPI -> PlaceholderAPIService()
            PlaceholderMods.MINIPLACEHOLDERS -> MiniPlaceholdersService()
        }
    }
}
