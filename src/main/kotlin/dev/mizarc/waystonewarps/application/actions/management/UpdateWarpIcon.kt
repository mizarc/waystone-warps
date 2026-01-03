package dev.mizarc.waystonewarps.application.actions.management

import IconMeta
import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class UpdateWarpIcon(private val warpRepository: WarpRepository,
                     private val warpEventPublisher: WarpEventPublisher) {
    fun execute(
        editorPlayerId: UUID,
        warpId: UUID,
        materialName: String,
        iconMeta: IconMeta = IconMeta(),
        bypassOwnership: Boolean = false,
    ): Result<Unit> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))

        // Check if the editor owns the warp
        if (warp.playerId != editorPlayerId && !bypassOwnership) {
            return Result.failure(Exception("Not authorized"))
        }

        val oldWarp = warp.copy()
        warp.icon = materialName
        warp.iconMeta = iconMeta
        warpRepository.update(warp)
        warpEventPublisher.warpModified(oldWarp, warp)
        return Result.success(Unit)
    }
}