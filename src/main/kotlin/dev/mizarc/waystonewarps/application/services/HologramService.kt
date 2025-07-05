package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp

interface HologramService {
    fun spawnHologram(warp: Warp)
    fun updateHologram(warp: Warp)
    fun removeHologram(warp: Warp)
}