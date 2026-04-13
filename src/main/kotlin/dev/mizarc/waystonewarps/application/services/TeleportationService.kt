package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.application.results.TeleportResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.UUID

/**
 * Service that handles the teleportation of players.
 */
interface TeleportationService {
    /**
     * Teleports the player to a waystone.
     *
     * @param playerId The id of the player to query.
     * @param warp The waystone to teleport to.
     */
    fun teleportPlayer(playerId: UUID, warp: Warp): TeleportResult

    /**
     * Schedules the teleportation to be done after a set amount of time.
     *
     * @param playerId The id of the player.
     * @param warp The waystone to teleport the player to.
     * @param delaySeconds The time it should take to teleport the player.
     * @param onSuccess The callback to be performed when the teleportation is done instantaneously.
     * @param onPending The callback to be performed when the teleportation is pending.
     * @param onInsufficientFunds The callback to be performed when the player doesn't have enough funds to teleport.
     * @param onCanceled The callback to be performed when the teleportation is cancelled.
     * @param onFailure The callback to be performed if the teleport fails.
     */
    fun scheduleDelayedTeleport(playerId: UUID, warp: Warp, delaySeconds: Int, onSuccess: () -> Unit,
                                onPending: () -> Unit, onInsufficientFunds: () -> Unit, onCanceled: () -> Unit,
                                onWorldNotFound: () -> Unit, onLocked: () -> Unit, onFailure: () -> Unit,
                                onPermissionDenied: () -> Unit, onInterworldPermissionDenied: () -> Unit,
                                onCooldown: (secondsRemaining: Int) -> Unit)

    /**
     * Cancels a pending teleport.
     *
     * @param playerId The id of the player to cancel.
     * @return Whether there was a player to cancel.
     */
    fun cancelPendingTeleport(playerId: UUID): Result<Unit>

    /**
     * Calculates the cost for a player to teleport to a warp.
     * Only meaningful when the cost type is ITEM; otherwise returns the flat configured amount.
     *
     * @param playerId The id of the player.
     * @param warp The destination warp.
     * @return The calculated cost as a whole number.
     */
    fun calculateCost(playerId: UUID, warp: Warp): Int
}