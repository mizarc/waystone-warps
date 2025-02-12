package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class UpdateWarpName(private val warpRepository: WarpRepository) {
    fun execute(warpId: UUID, name: String): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))
        warp.name = name
        warpRepository.update(warp)
        return Result.success(Unit)
    }
}