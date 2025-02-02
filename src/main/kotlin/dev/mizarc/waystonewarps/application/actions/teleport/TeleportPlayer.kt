package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.MovementMonitorService
import dev.mizarc.waystonewarps.application.services.Scheduler
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.players.PlayerLimitRepository
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import java.util.*

class TeleportPlayer(
    private val scheduler: Scheduler,
    private val teleportationService: TeleportationService,
    private val movementMonitorService: MovementMonitorService,
    private val playerLimitRepository: PlayerLimitRepository
) {
    fun execute(playerId: UUID, waystone: Waystone): Result<Unit> {
        // Retrieve player settings
        val timer = playerLimitRepository.getTeleportTimer(playerId)

        return if (timer > 0) {
            scheduleDelayedTeleport(playerId, waystone, timer)
        } else {
            performInstantTeleport(playerId, waystone)
        }
    }

    private fun performInstantTeleport(playerId: UUID, waystone: Waystone): Result<Unit> {
        return teleportationService.teleportWaystone(playerId, waystone)
    }

    private fun scheduleDelayedTeleport(playerId: UUID, waystone: Waystone, delaySeconds: Int): Result<Unit> {
        // Schedule delayed teleportation
        val taskHandle = scheduler.schedule(delaySeconds * 20L) {
            teleportationService.teleportWaystone(playerId, waystone)
        }

        // Monitor for player movement to cancel teleportation
        movementMonitorService.monitorPlayerMovement(playerId) {
            taskHandle.cancel()
            // Notify player about cancellation
        }

        // Notify player about the delayed teleportation
        return Result.success(Unit)
    }
}
