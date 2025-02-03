package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.waystones.Waystone
import org.bukkit.entity.Player
import java.util.*

/**
 * Service that handles the teleportation of players.
 */
interface TeleportationService {
    /**
     * Teleports the player to a waystone.
     *
     * @param playerId The id of the player to query.
     * @param waystone The waystone to teleport to.
     */
    fun teleportPlayer(playerId: UUID, waystone: Waystone): Result<Unit>

    /**
     * Schedules the teleportation to be done after a set amount of time.
     *
     * @param playerId The id of the player.
     * @param waystone The waystone to teleport the player to.
     * @param delaySeconds The time it should take to teleport the player.
     * @param onSuccess The callback to be performed when the teleportation is successful.
     * @param onCanceled The callback to be performed when the teleportation is cancelled.
     */
    fun scheduleDelayedTeleport(playerId: UUID, waystone: Waystone, delaySeconds: Int,
                                onSuccess: () -> Unit, onCanceled: () -> Unit): Result<Unit>

    /**
     * Cancels a pending teleport.
     *
     * @param playerId The id of the player to cancel.
     * @return Whether there was a player to cancel.
     */
    fun cancelPendingTeleport(playerId: UUID): Result<Unit>
}