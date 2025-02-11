package dev.mizarc.waystonewarps.application.actions.teleport

import dev.mizarc.waystonewarps.application.services.MovementMonitorService
import java.util.UUID

class LogPlayerMovement(private val movementMonitorService: MovementMonitorService) {

    fun execute(playerId: UUID) {
        movementMonitorService.logPlayerMovement(playerId)
    }
}