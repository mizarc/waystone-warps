package dev.mizarc.waystonewarps.domain.positioning

import org.bukkit.Location
import org.bukkit.World

/**
 * Stores two integers to define a 3D position in the world.
 *
 * @property x The X-Axis position.
 * @property y The Y-Axis position.
 * @property z The Z-Axis position.
 */
data class Position3D(val x: Int, val y: Int, val z: Int)