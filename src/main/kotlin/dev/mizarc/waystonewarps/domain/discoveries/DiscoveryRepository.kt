package dev.mizarc.waystonewarps.domain.discoveries

import java.util.UUID

/**
 * A repository that handles the persistence of warp discoveries.
 */
interface DiscoveryRepository {

    /**
     * Gets all players that have discovered a given warp.
     *
     * @param warpId The id of the warp to query.
     * @return The map of warps linked to each player.
     */
    fun getByWarp(warpId: UUID): Set<Discovery>

    /**
     * Gets all warps the player has discovered.
     *
     * @param playerId The player to query.
     * @return The set of warps that the player has discovered.
     */
    fun getByPlayer(playerId: UUID): Set<Discovery>

    /**
     * Gets the specific discover linked to the warp and player.
     *
     * @param playerId Unique id of the player to query.
     * @param warpId Unique id of the warp to query.
     * @return Discovery object if found.
     */
    fun getByWarpAndPlayer(warpId: UUID, playerId: UUID): Discovery?

    /**
     * Adds a discovery entry that links a player to a warp.
     *
     * @param discovery The discovery to add.
     */
    fun add(discovery: Discovery)

    /**
     * Updates the data of an existing discovery.
     *
     * @param discovery The discovery to update.
     */
    fun update(discovery: Discovery)

    /**
     * Removes the discovery link between a player and the warp.
     *
     * @param playerId Unique id of the player.
     * @param warpId Unique id of the warp.
     */
    fun remove(playerId: UUID, warpId: UUID)
}