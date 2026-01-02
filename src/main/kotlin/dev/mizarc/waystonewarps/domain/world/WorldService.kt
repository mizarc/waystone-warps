package dev.mizarc.waystonewarps.domain.world

import java.util.UUID

/**
 * Service for world-related operations.
 */
interface WorldService {
    /**
     * Checks if a world is invalid (doesn't exist or isn't loaded).
     *
     * @param worldId The UUID of the world to check
     * @return true if the world is invalid, false otherwise
     */
    fun isWorldInvalid(worldId: UUID): Boolean

    /**
     * Checks if a world name is invalid (doesn't exist or isn't loaded).
     *
     * @param worldName The name of the world to check
     * @return true if the world is invalid, false otherwise
     */
    fun isWorldInvalid(worldName: String): Boolean
}
