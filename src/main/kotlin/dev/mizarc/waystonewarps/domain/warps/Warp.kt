package dev.mizarc.waystonewarps.domain.warps

import IconMeta
import dev.mizarc.waystonewarps.domain.positioning.Position3D
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
           var position: Position3D, var icon: String, var iconMeta: IconMeta, var block: String, var isLocked: Boolean) {
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
        UUID.randomUUID(), playerId, Instant.now(), name, worldId, position, "LODESTONE", IconMeta(), block, false)

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

    /**
     * Creates a deep copy of this Warp object.
     * @return A new Warp instance with the same property values as this one.
     */
    fun copy(): Warp {
        return Warp(
            id = UUID.fromString(id.toString()),
            playerId = UUID.fromString(playerId.toString()),
            creationTime = Instant.ofEpochMilli(creationTime.toEpochMilli()),
            name = name,
            worldId = UUID.fromString(worldId.toString()),
            position = position.copy(),
            icon = icon,
            iconMeta = IconMeta(
                schemaVersion = iconMeta.schemaVersion,
                strings = iconMeta.strings.toList(),
                floats = iconMeta.floats.toList(),
                flags = iconMeta.flags.toList(),
                colorsArgb = iconMeta.colorsArgb.toList(),
                potionTypeKey = iconMeta.potionTypeKey,
                leatherColorRgb = iconMeta.leatherColorRgb,
                trimPatternKey = iconMeta.trimPatternKey,
                trimMaterialKey = iconMeta.trimMaterialKey,
                bannerBaseColor = iconMeta.bannerBaseColor,
                bannerPatterns = iconMeta.bannerPatterns.toList(),
                skullTextureValue = iconMeta.skullTextureValue,
                skullTextureSignature = iconMeta.skullTextureSignature,
                fireworkStarColorRgb = iconMeta.fireworkStarColorRgb
            ),
            block = block,
            isLocked = isLocked
        ).also {
            it.breakCount = this.breakCount
        }
    }
}