package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.*

class TeleportPlayer(private val teleportationService: TeleportationService,
        private val playerAttributeService: PlayerAttributeService) {

    fun execute(playerId: UUID, warp: Warp, onPending: (delaySeconds: Int) -> Unit, onSuccess: () -> Unit,
                onCanceled: () -> Unit, onFailure: (reason: String) -> Unit) {
        // Retrieve player settings
        val timer = playerAttributeService.getTeleportTimer(playerId)

        // Schedule delayed teleport
        if (timer > 0) {
            val result = teleportationService.scheduleDelayedTeleport(playerId, warp, timer, onSuccess, onCanceled)
            if (result.isSuccess) {
                onPending(timer)
            } else {
                onFailure("Failed to schedule delayed teleport")
            }
            return
        }

        // Instant teleport
        val result = teleportationService.teleportPlayer(playerId, warp)
        if (result.isSuccess) {
            onSuccess()
        } else {
            onFailure("Failed to teleport")
        }
    }
}
