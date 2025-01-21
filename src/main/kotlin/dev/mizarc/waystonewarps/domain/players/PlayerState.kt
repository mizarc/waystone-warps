package dev.mizarc.waystonewarps.domain.players

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * Holds temporary player state data.
 *
 * @property player The reference to the player.
 */
class PlayerState(val player: OfflinePlayer) {
    var isTeleporting = false
    var lastTeleportTime = 0

    /**
     * Gets the online version of the player instance.
     *
     * @return The online player instance.
     */
    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }
}