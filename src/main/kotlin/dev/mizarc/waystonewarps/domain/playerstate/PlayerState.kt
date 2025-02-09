package dev.mizarc.waystonewarps.domain.playerstate

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Holds temporary player state data.
 *
 * @property playerId The id of the player to store state of.
 */
class PlayerState(val playerId: UUID) {
    var isTeleporting = false
    var lastTeleportTime = 0
    var override = false
}