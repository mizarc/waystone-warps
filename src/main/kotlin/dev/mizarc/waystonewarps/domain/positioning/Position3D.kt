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
data class Position3D(val x: Int, val y: Int, val z: Int) {
    /**
     * Creates a position from a location.
     *
     * @param location The location instance to use.
     */
    constructor(location: Location): this(location.blockX, location.blockY, location.blockZ)

    /**
     * Converts the position to a location.
     *
     * @param world The world to use.
     * @return The combined location instance.
     */
    fun toLocation(world: World): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }
}