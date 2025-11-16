package dev.mizarc.waystonewarps.application.actions.whitelist

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.whitelist.Whitelist
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import java.util.UUID

class ToggleWhitelist(
    private val whitelistRepository: WhitelistRepository,
    private val warpRepository: WarpRepository
) {

    fun execute(editorPlayerId: UUID, warpId: UUID, targetPlayerId: UUID): Result<Boolean> {
        val warp = warpRepository.getById(warpId) ?: return Result.failure(Exception("Warp not found"))

        // Check if the editor owns the warp
        if (warp.playerId != editorPlayerId) {
            return Result.failure(Exception("Not authorized"))
        }

        val result = if (whitelistRepository.isWhitelisted(warpId, targetPlayerId)) {
            whitelistRepository.remove(warpId, targetPlayerId)
            false
        } else {
            whitelistRepository.add(Whitelist(warpId, targetPlayerId))
            true
        }

        return Result.success(result)
    }
}