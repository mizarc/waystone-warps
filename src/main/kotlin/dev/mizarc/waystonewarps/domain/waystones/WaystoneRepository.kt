package dev.mizarc.waystonewarps.domain.waystones

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * A repository that handles the persistence of Waystones.
 */
interface WaystoneRepository {
    /**
     * Gets all waystones that exist.
     *
     * @return The set of all waystones.
     */
    fun getAll(): Set<Waystone>

    /**
     * Gets a waystone by its id.
     *
     * @param id the unique id of the waystone.
     * @return The found waystone, or null if it doesn't exist.
     */
    fun getById(id: UUID): Waystone?

    /**
     * Gets all waystones that a player owns.
     *
     * @param player The player to retrieve waystones for.
     * @return A set of waystones owned by the player.
     */
    fun getByPlayer(playerId: UUID): List<Waystone>

    /**
     * Retrieves a waystone by the position in the world.
     *
     * @param position The position in the world.
     * @param worldId The unique id of the world.
     * @return The found waystone, or null if it doesn't exist.
     */
    fun getByPosition(position: Position3D, worldId: UUID): Waystone?

    /**
     * Adds a new waystone.
     *
     * @param waystone The waystone to add.
     */
    fun add(waystone: Waystone)

    /**
     * Updates the data of an existing waystone.
     *
     * @param waystone The waystone to update.
     */
    fun update(waystone: Waystone)

    /**
     * Removes an existing waystone.
     *
     * @param waystone The waystone to remove.
     */
    fun remove(waystone: Waystone)
}