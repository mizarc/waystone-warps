package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class UpdateWarpIcon(private val warpRepository: WarpRepository) {
    fun execute(editorPlayerId: UUID, warpId: UUID, materialName: String): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))

        // Check if the editor owns the warp
        if (warp.playerId != editorPlayerId) {
            return Result.failure(Exception("Not authorized"))
        }

        warp.icon = materialName
        warpRepository.update(warp)
        return Result.success(Unit)
    }
}