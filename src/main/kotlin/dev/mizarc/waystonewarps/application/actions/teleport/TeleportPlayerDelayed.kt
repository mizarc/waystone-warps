package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.MessagingService
import dev.mizarc.waystonewarps.application.services.MovementMonitorService
import dev.mizarc.waystonewarps.application.services.Scheduler
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import java.util.*

class TeleportPlayerDelayed(
    private val scheduler: Scheduler,
    private val teleportationService: TeleportationService,
    private val movementMonitorService: MovementMonitorService,
    private val messagingService: MessagingService
) {
    fun execute(playerId: UUID, waystone: Waystone, delaySeconds: Int): Result<Unit> {
        // Schedule delayed teleportation
        val taskHandle = scheduler.schedule(delaySeconds * 20L) {
            teleportationService.teleportWaystone(playerId, waystone)
            movementMonitorService.stopMonitoringPlayer(playerId)
        }

        // Monitor for player movement to cancel teleportation
        movementMonitorService.monitorPlayerMovement(playerId) {
            taskHandle.cancel()
            movementMonitorService.stopMonitoringPlayer(playerId)
            messagingService.sendActionMessage(playerId, "Teleportation canceled because you moved.")
        }

        // Notify player about the delayed teleportation
        messagingService.sendActionMessage(playerId, "Teleportation will commence in $delaySeconds seconds. Please stand still.")

        return Result.success(Unit)
    }
}
