package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class ToggleLock(private val warpRepository: WarpRepository) {
    fun execute(playerId: UUID, warpId: UUID): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))

        // Check if the player owns the warp
        if (warp.playerId != playerId) {
            return Result.failure(Exception("Not authorized"))
        }

        warp.isLocked = !warp.isLocked
        warpRepository.update(warp)
        return Result.success(Unit)
    }
}