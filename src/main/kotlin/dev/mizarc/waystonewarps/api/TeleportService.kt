package dev.mizarc.waystonewarps.api

import dev.mizarc.waystonewarps.domain.waystones.Waystone
import org.bukkit.entity.Player

/**
 * Service that handles the teleportation of players.
 */
interface TeleportService {
    /**
     * Teleports the player to a waystone.
     *
     * @param player The player to query.
     * @param waystone The waystone to teleport to.
     */
    fun teleportWaystone(player: Player, waystone: Waystone, sendAlert: Boolean = true)
}