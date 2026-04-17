package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.UUID

/**
 * Provides Service for creating bossbar based counbtdown timers for player teleports.
 */
interface PlayerCountdownService {
    /**
     * Starts countdown for the player.
     *
     * @param playerId The id of the player to start countdown for.
     * @param warp The waystone to which user will be teleported.
     */
    fun startCountdown(playerId: UUID, warp: Warp)

    /**
     * Cancels countdown for the player.
     *
     * @param playerId The id of the player to cancel countdown for.
     */
    fun cancelCountdown(playerId: UUID)
}