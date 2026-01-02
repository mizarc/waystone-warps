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
     * Gets a warp by its id.
     *
     * @param id the unique id of the warp.
     * @return The found warp, or null if it doesn't exist.
     */
    fun getById(id: UUID): Warp?

    /**
     * Gets all warps that a player owns.
     *
     * @param playerId The player to retrieve the warp for.
     * @return A set of warps owned by the player.
     */
    fun getByPlayer(playerId: UUID): List<Warp>


    /**
     * Gets all warps for a particular world.
     *
     * @param worldId The world to retrieve the warps for.
     * @return A set of warps in the world.
     */
    fun getByWorld(worldId: UUID): List<Warp>

    /**
     * Gets a warp owned by a player by name.
     *
     * @param playerId The player that owns the warp.
     * @param name The name of the warp.
     * @return The warp owned by the player.
     */
    fun getByName(playerId: UUID, name: String): Warp?

    /**
     * Retrieves a warp by the position in the world.
     *
     * @param position The position in the world.
     * @param worldId The unique id of the world.
     * @return The found warp, or null if it doesn't exist.
     */
    fun getByPosition(position: Position3D, worldId: UUID): Warp?

    /**
     * Adds a new warp.
     *
     * @param warp The warp to add.
     */
    fun add(warp: Warp)

    /**
     * Updates the data of an existing warp.
     *
     * @param warp The warp to update.
     */
    fun update(warp: Warp)

    /**
     * Removes an existing warp.
     *
     * @param id The id of the warp to remove.
     */
    fun remove(id: UUID)
}