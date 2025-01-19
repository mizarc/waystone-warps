package dev.mizarc.worldwidewarps.domain

import dev.mizarc.worldwidewarps.Direction
import dev.mizarc.worldwidewarps.Position
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

/**
 * Stores a warp, a public global teleportation system.
 * @property id The unique identifier.
 * @property player The player that owns the warp.
 * @property name The name of the warp.
 * @property world The world the warp is in.
 * @property position The position in the world.
 * @property direction The facing direction
 */
data class  Warp(val id: UUID, val player: OfflinePlayer, val creationTime: Instant, var name: String, var worldId: UUID,
                var position: Position, var direction: Direction, var icon: Material) {
    val defaultBreakCount = 3
    var breakCount = 3
    var breakPeriod = false

    /**
     * Used to create a new warp instance with an auto generated UUID.
     * @param player The player that owns the warp.
     * @param name The name of the warp.
     * @param world The world the warp is in.
     * @param position The position in the world.
     */
    constructor(player: OfflinePlayer, name: String, worldId: UUID, position: Position,
                direction: Direction, icon: Material):
            this(UUID.randomUUID(), player, Instant.now(), name, worldId, position, direction, icon)

    constructor(builder: Builder):
            this(UUID.randomUUID(), builder.player, Instant.now(), builder.name, builder.world.uid,
                builder.position, builder.direction, builder.icon)

    fun getWorld(): World? {
        return Bukkit.getWorld(worldId)
    }

    class Builder(val player: Player, val world: World, val position: Position) {
        var name = "Warp"
        var direction = Direction.NORTH
        var icon = Material.LODESTONE

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun build() = Warp(this)
    }

    fun resetBreakCount() {
        if (!breakPeriod) {
            thread(start = true) {
                breakPeriod = true
                Thread.sleep(10000)
                breakCount = defaultBreakCount
                breakPeriod = false
            }
        }
    }
}
