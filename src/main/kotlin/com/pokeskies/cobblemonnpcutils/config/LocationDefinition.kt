package com.pokeskies.cobblemonnpcutils.config

import com.pokeskies.cobblemonnpcutils.utils.Utils
import net.minecraft.world.entity.player.Player

class LocationDefinition(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float? = null,
    val pitch: Float? = null
) {
    fun teleportPlayer(player: Player): Boolean {
        val level = Utils.getLevel(world) ?: return false
        return player.teleportTo(level, x, y, z, emptySet(), yaw ?: player.yRot, pitch ?: player.xRot)
    }
}
