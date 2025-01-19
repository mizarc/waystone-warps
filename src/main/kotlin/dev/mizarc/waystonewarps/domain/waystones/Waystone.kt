package dev.mizarc.waystonewarps.domain.waystones

import dev.mizarc.waystonewarps.domain.positioning.Position3D
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
class Waystone(val id: UUID, val player: OfflinePlayer, val creationTime: Instant, var name: String, var worldId: UUID,
               var position: Position3D, var icon: Material) {

    /**
     * Compiles a new waystone based on the minimum details required.
     *
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param owner The player who owns the claim.
     * @param position The position of the claim.
     * @param name The name of the claim.
     */
    constructor(worldId: UUID, owner: OfflinePlayer, position: Position3D, name: String) : this(
        UUID.randomUUID(), owner, Instant.now(), name, worldId, position, Material.BELL)

    /**
     * Compiles a waystone based on waystone builder object data.
     *
     * @param builder The waystone builder to build a waystone out of.
     */
    constructor(builder: Builder): this(builder.location.world.uid, builder.player,
        Position3D(builder.location), builder.name)

    /**
     * A builder for creating instances of a Waystone.
     *
     * @property player The player who should own the waystone.
     * @property location The location the waystone should exist in.
     */
    class Builder(val player: Player, val location: Location) {
        var name = ""

        fun build() = Waystone(this)
    }
}