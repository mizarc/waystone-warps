package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp

interface StructureParticleService {
    fun spawnParticles(warp: Warp, particleName: String, spawnSpeed: Long)
    fun removeParticles(warp: Warp)
}