package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.UUID

interface PlayerParticleService {
    fun spawnParticles(playerId: UUID)
    fun removeParticles(playerId: UUID)
}