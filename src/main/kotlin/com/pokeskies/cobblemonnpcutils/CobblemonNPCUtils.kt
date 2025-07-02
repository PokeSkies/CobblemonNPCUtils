package com.pokeskies.cobblemonnpcutils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import com.pokeskies.cobblemonnpcutils.commands.BaseCommand
import com.pokeskies.cobblemonnpcutils.config.ConfigManager
import com.pokeskies.cobblemonnpcutils.economy.EconomyType
import com.pokeskies.cobblemonnpcutils.economy.IEconomyService
import com.pokeskies.cobblemonnpcutils.utils.MolangUtils
import com.pokeskies.cobblemonnpcutils.utils.Utils
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.api.ModInitializer
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

class CobblemonNPCUtils : ModInitializer {
    companion object {
        lateinit var INSTANCE: CobblemonNPCUtils

        var MOD_ID = "cobblemonnpcutils"
        var MOD_NAME = "CobblemonNPCUtils"

        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        val asyncScope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun asResource(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    lateinit var configDir: File

    lateinit var adventure: FabricServerAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    private var economyServices: Map<EconomyType, IEconomyService> = emptyMap()

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("CobblemonNPCUtils-Async-%d")
        .setDaemon(true)
        .build())

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeHierarchyAdapter(CompoundTag::class.java, Utils.CodecSerializer(CompoundTag.CODEC))
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, MOD_ID)
        ConfigManager.load()

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)

            MolangUtils.setupMolang()
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
        }
    }

    fun reload() {
        ConfigManager.load()

        this.economyServices = IEconomyService.getLoadedEconomyServices()
    }

    fun getLoadedEconomyServices(): Map<EconomyType, IEconomyService> {
        return this.economyServices
    }

    fun getEconomyService(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] }
    }

    fun getEconomyServiceOrDefault(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] } ?: this.economyServices.values.firstOrNull()
    }
}
