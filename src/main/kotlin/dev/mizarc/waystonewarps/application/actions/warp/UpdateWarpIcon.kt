package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class UpdateWarpIcon(private val warpRepository: WarpRepository) {
    fun execute(warpId: UUID, materialName: String): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))
        warp.icon = materialName
        warpRepository.update(warp)
        return Result.success(Unit)
    }
}