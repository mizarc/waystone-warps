package dev.mizarc.waystonewarps.api

/**
 * A service that allows you to get player limits.
 */
interface PlayerLimitService {
    /**
     * Gets the total number of waystones a player can create.
     */
    fun getWaystoneLimit(): Int

    /**
     * Gets the player's teleport cost for teleporting to a waystone.
     */
    fun getTeleportCost(): Int

    /**
     * Gets the player's wait time for teleporting to a waystone.
     */
    fun getTeleportTimer(): Int
}