package dev.mizarc.waystonewarps.domain.warps

import CustomModelData
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import org.bukkit.Material
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

/**
 * Stores Warp information.
 * @property id The unique identifier.
 * @property playerId The player that owns the warp.
 * @property name The name of the warp.
 * @property worldId The world the warp is in.
 * @property position The position in the world.
 * @property icon The name of the material to use as an icon.
 */
class Warp(val id: UUID, val playerId: UUID, val creationTime: Instant, var name: String, var worldId: UUID,
           var position: Position3D, var icon: String, var iconMeta: CustomModelData, var block: String, var isLocked: Boolean) {
    var breakCount = 3

    private val defaultBreakCount = 3
    private var breakPeriod = false

    /**
     * Compiles a new warp based on the minimum details required.
     *
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param playerId The id of the player who owns the warp.
     * @param position The position of the warp.
     * @param name The name of the claim.
     * @param block The base block being used for the physical appearance.
     */
    constructor(worldId: UUID, playerId: UUID, position: Position3D, name: String, block: String) : this(
        UUID.randomUUID(), playerId, Instant.now(), name, worldId, position, "LODESTONE", CustomModelData(), block, false)

    /**
     * Resets the break count after a set period of time.
     */
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