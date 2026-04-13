package dev.mizarc.waystonewarps.domain.playerstate

import java.util.UUID

/**
 * Holds temporary player state data.
 *
 * @property playerId The id of the player to store state of.
 */
class PlayerState(val playerId: UUID) {
    var isTeleporting = false
    var lastTeleportTime = 0L
    var override = false
}
