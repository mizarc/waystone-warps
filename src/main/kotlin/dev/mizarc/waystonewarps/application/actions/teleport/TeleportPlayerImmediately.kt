package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.*

class TeleportPlayerImmediately(private val teleportationService: TeleportationService) {
    fun execute(playerId: UUID, warp: Warp): Result<Unit> {
        return teleportationService.teleportPlayer(playerId, warp)
    }
}
