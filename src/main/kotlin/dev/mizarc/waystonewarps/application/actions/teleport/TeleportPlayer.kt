package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.players.PlayerLimitRepository
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import java.util.*

class TeleportPlayer(private val teleportationService: TeleportationService,
        private val playerLimitRepository: PlayerLimitRepository) {

    fun execute(playerId: UUID, waystone: Waystone, onDelayed: (delaySeconds: Int) -> Unit, onSuccess: () -> Unit,
                onCanceled: () -> Unit, onFailure: (reason: String) -> Unit) {
        // Retrieve player settings
        val timer = playerLimitRepository.getTeleportTimer(playerId)

        // Schedule delayed teleport
        if (timer > 0) {
            val result = teleportationService.scheduleDelayedTeleport(playerId, waystone, timer, onSuccess, onCanceled)
            if (result.isSuccess) {
                onDelayed(timer)
            } else {
                onFailure("Failed to schedule delayed teleport")
            }
            return
        }

        // Instant teleport
        val result = teleportationService.teleportPlayer(playerId, waystone)
        if (result.isSuccess) {
            onSuccess()
        } else {
            onFailure("Failed to teleport")
        }
    }
}
