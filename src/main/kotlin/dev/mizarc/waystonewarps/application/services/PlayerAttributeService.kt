package dev.mizarc.waystonewarps.application.services

import java.util.*

/**
 * A server that handles read only player attributes.
 *
 * A permission provider is expected to handle this, so no per player internal storage is available.
 */
interface PlayerAttributeService {

    /**
     * Gets the amount of warps a player is allowed to own.
     *
     * @param playerId The target player's id.
     * @returns The amount of warps.
     */
    fun getWarpLimit(playerId: UUID): Int

    /**
     * Gets the cost that the player will incur for teleport to a waystone.
     *
     * @param playerId The target player's id.
     * @returns The cost amount.
     */
    fun getTeleportCost(playerId: UUID): Int

    /**
     * Gets how long it takes for the player to teleport.
     *
     * @param playerId The target player's id.
     * @returns The time it takes to teleport.
     */
    fun getTeleportTimer(playerId: UUID): Int
}
