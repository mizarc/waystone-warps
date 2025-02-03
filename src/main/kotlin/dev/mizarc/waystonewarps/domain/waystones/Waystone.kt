package dev.mizarc.waystonewarps.domain.waystones

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*

/**
 * Stores Waystone information.
 * @property id The unique identifier.
 * @property player The player that owns the warp.
 * @property name The name of the warp.
 * @property worldId The world the warp is in.
 * @property position The position in the world.
 */
class Waystone(val id: UUID, val playerId: UUID, val creationTime: Instant, var name: String, var worldId: UUID,
               var position: Position3D, var icon: Material) {

    /**
     * Compiles a new waystone based on the minimum details required.
     *
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param playerId The id of the player who owns the warp.
     * @param position The position of the warp.
     * @param name The name of the claim.
     */
    constructor(worldId: UUID, playerId: UUID, position: Position3D, name: String) : this(
        UUID.randomUUID(), playerId, Instant.now(), name, worldId, position, Material.BELL)
}