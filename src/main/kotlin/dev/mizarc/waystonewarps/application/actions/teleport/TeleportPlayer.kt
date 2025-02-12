package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.results.TeleportResult
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.*

class TeleportPlayer(private val teleportationService: TeleportationService,
        private val playerAttributeService: PlayerAttributeService) {

    fun execute(playerId: UUID, warp: Warp, onSuccess: () -> Unit, onPending: () -> Unit,
                onInsufficientFunds: () -> Unit, onCanceled: () -> Unit, onWorldNotFound: () -> Unit,
                onFailure: () -> Unit) {
        // Retrieve player settings
        val timer = playerAttributeService.getTeleportTimer(playerId)

        // Schedule delayed teleport
        if (timer > 0) {
            teleportationService.scheduleDelayedTeleport(playerId, warp, timer, onSuccess, onPending,
                onInsufficientFunds, onCanceled, onWorldNotFound, onFailure)
            return
        }

        // Instant teleport
        val result = teleportationService.teleportPlayer(playerId, warp)
        when (result) {
            TeleportResult.SUCCESS -> onSuccess()
            TeleportResult.INSUFFICIENT_FUNDS -> onInsufficientFunds()
            TeleportResult.WORLD_NOT_FOUND -> onWorldNotFound()
            TeleportResult.FAILED -> onFailure()
        }
    }
}
