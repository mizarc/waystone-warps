package dev.mizarc.worldwidewarps.domain

import dev.geco.gsit.objects.IGPoseSeat
import dev.mizarc.worldwidewarps.Direction
import dev.mizarc.worldwidewarps.Position
import org.bukkit.DyeColor
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.data.type.Bed
import org.bukkit.entity.Player
import dev.mizarc.worldwidewarps.utils.getColour
import org.bukkit.Bukkit
import java.time.Instant
import java.util.*

/**
 * Stores a home consisting of a name and a position.
 * @property id The unique identifier.
 * @property player The player that owns the home.
 * @property name The name of the home.
 * @property colour The colour of the bed.
 * @property position The position in the world.
 */
class Home(val id: UUID, val player: OfflinePlayer, val creationTime: Instant, val name: String, val colour: DyeColor,
           val worldId: UUID, val position: Position, val direction: Direction
) {

    /**
     * Used to create a new home instance with an auto generated UUID.
     * @param player The player that owns the home.
     * @param name The name of the home.
     * @param colour The colour of the bed.
     * @param position The position in the world.
     */
    constructor(player: OfflinePlayer, name: String, colour: DyeColor,
                worldId: UUID, position: Position, direction: Direction
    ):
            this(UUID.randomUUID(), player, Instant.now(), name, colour, worldId, position, direction)

    constructor(builder: Builder):
            this(UUID.randomUUID(), builder.player, Instant.now(), builder.name, builder.bed.getColour(),
                builder.world.uid, builder.position, builder.direction)

    fun getWorld(): World? {
        return Bukkit.getWorld(worldId)
    }

    class Builder(val player: Player, val world: World, val position: Position, val bed: Bed) {
        var name = ""
        var sleep = true
        lateinit var pose: IGPoseSeat
        val direction: Direction = Direction.fromVector(bed.facing.oppositeFace.direction)

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun sleep(state: Boolean): Builder {
            this.sleep = state
            return this
        }

        fun pose(pose: IGPoseSeat): Builder {
            this.pose = pose
            return this
        }

        fun build() = Home(this)
    }
}
