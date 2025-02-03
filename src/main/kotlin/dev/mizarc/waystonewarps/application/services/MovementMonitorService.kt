package dev.mizarc.waystonewarps.application.services

import java.util.UUID

interface MovementMonitorService {
    fun monitorPlayerMovement(playerId: UUID, onMove: () -> Unit)
    fun stopMonitoringPlayer(playerId: UUID)
    fun logPlayerMovement(playerId: UUID)
}