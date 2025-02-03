package dev.mizarc.waystonewarps.domain.discoveries

import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.UUID

/**
 * A repository that handles the persistence of warp discoveries.
 */
interface DiscoveryRepository {

    /**
     * Gets all players that have discovered a given warp.
     *
     * @param warp The warp to query.
     * @return The map of warps linked to each player.
     */
    fun getByWarp(warp: Warp): Set<Discovery>

    /**
     * Gets all warps the player has discovered.
     *
     * @param playerId The player to query.
     * @return The set of warps that the player has discovered.
     */
    fun getByPlayer(playerId: UUID): Set<Discovery>

    /**
     * Adds a discovery entry that links a player to a warp.
     *
     * @param discovery The discovery to add.
     */
    fun add(discovery: Discovery)

    /**
     * Removes the discovery link between a player and the warp.
     *
     * @param discovery The discovery to remove.
     */
    fun remove(discovery: Discovery)
}