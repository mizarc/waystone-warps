package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import java.util.*

class TeleportPlayerImmediately(private val teleportationService: TeleportationService) {
    fun execute(playerId: UUID, waystone: Waystone): Result<Unit> {
        return teleportationService.teleportPlayer(playerId, waystone)
    }
}
