package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.MovementMonitorService
import java.util.*

class MovementMonitorServiceBukkit : MovementMonitorService {
    private val monitoredPlayers = mutableMapOf<UUID, () -> Unit>()

    override fun monitorPlayerMovement(playerId: UUID, onMove: () -> Unit) {
        monitoredPlayers[playerId] = onMove
    }

    override fun stopMonitoringPlayer(playerId: UUID) {
        monitoredPlayers.remove(playerId)
    }

    override fun logPlayerMovement(playerId: UUID) {
        val onMove = monitoredPlayers[playerId]
        if (onMove != null) {
            onMove.invoke()
            stopMonitoringPlayer(playerId)
        }
    }
}
