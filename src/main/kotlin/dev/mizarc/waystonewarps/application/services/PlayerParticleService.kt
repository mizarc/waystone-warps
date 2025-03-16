package dev.mizarc.waystonewarps.application.services

import java.util.UUID

interface PlayerParticleService {
    fun spawnPreParticles(playerId: UUID)
    fun spawnPostParticles(playerId: UUID)
    fun removeParticles(playerId: UUID)
}