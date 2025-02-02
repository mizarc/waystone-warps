package dev.mizarc.waystonewarps.domain.players

import org.bukkit.OfflinePlayer

/**
 * A repository that handles read only player limits.
 *
 * This implementation uses the Vault API to store limits via a third party provider (e.g. LuckPerms)
 */
interface PlayerLimitRepository {

    /**
     * Gets the amount of waystones a player is allowed to own.
     *
     * @param player The target player
     * @returns The amount of waystones.
     */
    fun getWaystoneLimit(player: OfflinePlayer): Int

    /**
     * Gets the cost that the player will incur for teleport to a waystone.
     *
     * @param player The target player.
     * @returns The cost amount.
     */
    fun getTeleportCost(player: OfflinePlayer): Int

    /**
     * Gets how long it takes for the player to teleport.
     *
     * @param player The target player.
     * @returns The time it takes to teleport.
     */
    fun getTeleportTimer(player: OfflinePlayer): Int
}