package dev.mizarc.waystonewarps.domain.warps

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import java.util.*

/**
 * A repository that handles the persistence of Waystones.
 */
interface WarpRepository {
    /**
     * Gets all waystones that exist.
     *
     * @return The set of all waystones.
     */
    fun getAll(): Set<Warp>

    /**
     * Gets a waystone by its id.
     *
     * @param id the unique id of the waystone.
     * @return The found waystone, or null if it doesn't exist.
     */
    fun getById(id: UUID): Warp?

    /**
     * Gets all waystones that a player owns.
     *
     * @param playerId The player to retrieve waystones for.
     * @return A set of waystones owned by the player.
     */
    fun getByPlayer(playerId: UUID): List<Warp>

    /**
     * Retrieves a waystone by the position in the world.
     *
     * @param position The position in the world.
     * @param worldId The unique id of the world.
     * @return The found waystone, or null if it doesn't exist.
     */
    fun getByPosition(position: Position3D, worldId: UUID): Warp?

    /**
     * Adds a new waystone.
     *
     * @param warp The waystone to add.
     */
    fun add(warp: Warp)

    /**
     * Updates the data of an existing waystone.
     *
     * @param warp The waystone to update.
     */
    fun update(warp: Warp)

    /**
     * Removes an existing waystone.
     *
     * @param id The id of the waystone to remove.
     */
    fun remove(id: UUID)
}