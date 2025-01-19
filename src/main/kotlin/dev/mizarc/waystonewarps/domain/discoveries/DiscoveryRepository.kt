package dev.mizarc.waystonewarps.domain.discoveries

import dev.mizarc.waystonewarps.domain.waystones.Waystone
import org.bukkit.OfflinePlayer

/**
 * A repository that handles the persistence of waystone discoveries.
 */
interface DiscoveryRepository {

    /**
     * Gets all players that have discovered a given waystone.
     *
     * @param waystone The waystone to query.
     * @return The map of waystones linked to each player.
     */
    fun getByWaystone(waystone: Waystone): Set<OfflinePlayer>

    /**
     * Gets all waystones the player has discovered.
     *
     * @param player The player to query.
     * @return The set of waystones that the player has discovered.
     */
    fun getByPlayer(player: OfflinePlayer): Set<Waystone>

    /**
     * Adds an entry that links a player to a waystone.
     *
     * @param waystone The target waystone.
     * @param player The player to give the permission to.
     */
    fun add(waystone: Waystone, player: OfflinePlayer)

    /**
     * Removes the link between a player and the waystone.
     *
     * @param waystone The target waystone.
     * @param player The player to remove the permission from.
     */
    fun remove(waystone: Waystone, player: OfflinePlayer)
}