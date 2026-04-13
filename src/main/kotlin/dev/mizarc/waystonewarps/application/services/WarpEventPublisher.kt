package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.UUID

interface WarpEventPublisher {
    fun warpCreated(warp: Warp)
    fun warpDeleted(warp: Warp)
    fun warpDiscovered(playerId: UUID, warpId: UUID)
    fun warpModified(oldWarp: Warp, newWarp: Warp)
    fun warpTeleported(playerId: UUID, warp: Warp)
    fun warpUsed(playerId: UUID, warp: Warp)
}
