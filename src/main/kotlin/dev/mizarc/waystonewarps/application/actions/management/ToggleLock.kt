package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class ToggleLock(private val warpRepository: WarpRepository) {
    fun execute(warpId: UUID): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))
        warp.isLocked = !warp.isLocked
        warpRepository.update(warp)
        return Result.success(Unit)
    }
}