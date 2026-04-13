package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.warps.WarpAccess
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class ToggleLock(private val warpRepository: WarpRepository,
                 private val warpEventPublisher: WarpEventPublisher) {
    fun execute(playerId: UUID, warpId: UUID, bypassOwnership: Boolean = false, canSetServer: Boolean = false): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))

        // Check if the player owns the warp
        if (warp.playerId != playerId && !bypassOwnership) {
            return Result.failure(Exception("Not authorized"))
        }

        // Only server admins can change a SERVER warp
        if (warp.accessLevel == WarpAccess.SERVER && !canSetServer) {
            return Result.failure(Exception("Insufficient permissions to change a server warp"))
        }

        val oldWarp = warp.copy()
        warp.accessLevel = when (warp.accessLevel) {
            WarpAccess.PUBLIC -> WarpAccess.PRIVATE
            WarpAccess.PRIVATE -> if (canSetServer) WarpAccess.SERVER else WarpAccess.PUBLIC
            WarpAccess.SERVER -> WarpAccess.PUBLIC
        }
        warpRepository.update(warp)
        warpEventPublisher.warpModified(oldWarp, warp)
        return Result.success(Unit)
    }
}