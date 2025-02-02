package dev.mizarc.waystonewarps.domain.players

import org.bukkit.OfflinePlayer
import java.util.*

/**
 * A repository that handles read only player limits.
 *
 * A permission provider is expected to handle this, so no per player internal storage is available.
 */
interface PlayerLimitRepository {

    /**
     * Gets the amount of waystones a player is allowed to own.
     *
     * @param player The target player
     * @returns The amount of waystones.
     */
    fun getWaystoneLimit(playerId: UUID): Int

    /**
     * Gets the cost that the player will incur for teleport to a waystone.
     *
     * @param player The target player.
     * @returns The cost amount.
     */
    fun getTeleportCost(playerId: UUID): Int

    /**
     * Gets how long it takes for the player to teleport.
     *
     * @param player The target player.
     * @returns The time it takes to teleport.
     */
    fun getTeleportTimer(playerId: UUID): Int
}