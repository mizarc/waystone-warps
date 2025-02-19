package dev.mizarc.waystonewarps.domain.whitelist

import java.util.UUID

interface WhitelistRepository {
    /**
     * Gets all players that are whitelist access to a warp.
     *
     * @param warpId Unique identifier of the warp.
     * @return The list of players that are whitelisted.
     */
    fun getByWarp(warpId: UUID): List<UUID>

    /**
     * Gets all warps a player has whitelist access to.
     *
     * @param playerId Unique identifier of the player.
     * @return The list of warps the player has whitelist access to.
     */
    fun getByPlayer(playerId: UUID): List<UUID>

    /**
     * Adds a new whitelist.
     *
     * @param whitelist The whitelist to add.
     */
    fun add(whitelist: Whitelist)

    /**
     * Removes an existing player that is whitelisted in a warp.
     *
     * @param warpId Unique identifier of the warp.
     * @param playerId Unique identifier of the player.
     */
    fun remove(warpId: UUID, playerId: UUID)

    /**
     * Removes all whitelists for a given warp.
     *
     * @param warpId Unique identifier of the warp.
     */
    fun removeByWarp(warpId: UUID)
}