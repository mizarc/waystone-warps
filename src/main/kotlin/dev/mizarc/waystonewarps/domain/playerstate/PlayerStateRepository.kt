package dev.mizarc.waystonewarps.domain.playerstate

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * A service that handles the temporary state of player data.
 */
interface PlayerStateRepository {
    /**
     * Checks if the given player is registered to hold state data.
     *
     * @param playerId The player id to query.
     * @return True if the player is registered.
     */
    fun isExists(playerId: UUID): Boolean

    /**
     * Gets all players that are currently registered.
     *
     * @return The set of all players that are registered.
     */
    fun getAll(): Set<PlayerState>

    /**
     * Gets a player's state by a given id.
     *
     * @param id The unique id of the player.
     * @return The player's state data.
     */
    fun getById(id: UUID): PlayerState?

    /**
     * Registers the player to hold state data.
     *
     * @param playerState the player state to register.
     */
    fun add(playerState: PlayerState)

    /**
     * Unregisters the player to remove their state data.
     *
     * @param playerId The id of the player to unregister.
     */
    fun remove(playerId: UUID)
}