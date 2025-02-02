package dev.mizarc.waystonewarps.domain.discoveries

import dev.mizarc.waystonewarps.domain.waystones.Waystone
import org.bukkit.OfflinePlayer
import java.util.UUID

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
    fun getByWaystone(waystone: Waystone): Set<Discovery>

    /**
     * Gets all waystones the player has discovered.
     *
     * @param player The player to query.
     * @return The set of waystones that the player has discovered.
     */
    fun getByPlayer(playerId: UUID): Set<Discovery>

    /**
     * Adds a discovery entry that links a player to a waystone.
     *
     * @param discovery The discovery to add.
     */
    fun add(discovery: Discovery)

    /**
     * Removes the discovery link between a player and the waystone.
     *
     * @param discovery The discovery to remove.
     */
    fun remove(discovery: Discovery)
}