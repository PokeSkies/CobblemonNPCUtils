package com.pokeskies.cobblemonnpcutils.config

import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.pokeskies.cobblemonnpcutils.CobblemonNPCUtils
import com.pokeskies.cobblemonnpcutils.utils.Utils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object ConfigManager {
    private var assetPackage = "assets/${CobblemonNPCUtils.MOD_ID}"

    private val ITEM_DEFINITION_TYPE = object : TypeToken<MutableMap<String, ItemDefinition>>() {}.type
    private val LOCATION_DEFINITION_TYPE = object : TypeToken<MutableMap<String, LocationDefinition>>() {}.type

    lateinit var CONFIG: CobblemonNPCUtilsConfig
    lateinit var ITEM_DEFINITIONS: MutableMap<String, ItemDefinition>
    lateinit var LOCATION_DEFINITIONS: MutableMap<String, LocationDefinition>

    fun load() {
        // Load defaulted configs if they do not exist
        copyDefaults()

        // Load all files
        CONFIG = loadFile("config.json", CobblemonNPCUtilsConfig())
        ITEM_DEFINITIONS = loadFile("items.json", mutableMapOf(), type = ITEM_DEFINITION_TYPE, create = true)
        LOCATION_DEFINITIONS = loadFile("locations.json", mutableMapOf(), type = LOCATION_DEFINITION_TYPE, create = true)
    }

    private fun copyDefaults() {
        val classLoader = CobblemonNPCUtils::class.java.classLoader

        CobblemonNPCUtils.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")
        attemptDefaultFileCopy(classLoader, "items.json")
        attemptDefaultFileCopy(classLoader, "locations.json")
    }

    fun <T : Any> loadFile(filename: String, default: T, type: Type? = null, path: String = "", create: Boolean = false): T {
        var dir = CobblemonNPCUtils.INSTANCE.configDir
        if (path.isNotEmpty()) {
            dir = dir.resolve(path)
        }
        val file = File(dir, filename)
        var value: T = default
        try {
            Files.createDirectories(CobblemonNPCUtils.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val actualType = type ?: default::class.java
                    value = CobblemonNPCUtils.INSTANCE.gsonPretty.fromJson(JsonReader(reader), actualType)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(CobblemonNPCUtils.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = CobblemonNPCUtils.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(CobblemonNPCUtils.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = CobblemonNPCUtils.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            file.mkdirs()
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = CobblemonNPCUtils.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.forEach { sourceFile ->
                        val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                        if (Files.isDirectory(sourceFile)) {
                            // Create subdirectories in the destination
                            destinationFile.mkdirs()
                        } else {
                            // Copy files to the destination
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }
}
