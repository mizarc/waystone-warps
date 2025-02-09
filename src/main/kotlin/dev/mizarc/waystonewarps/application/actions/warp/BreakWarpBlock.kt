package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.application.results.BreakWarpResult
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class BreakWarpBlock(private val warpRepository: WarpRepository,
                     private val playerStateRepository: PlayerStateRepository) {
    fun execute(position: Position3D, worldId: UUID): BreakWarpResult  {
        val warp = warpRepository.getByPosition(position, worldId) ?: return BreakWarpResult.WarpNotFound

        // Trigger warp break timer
        warp.resetBreakCount()

        // Warp has been broken
        warp.breakCount -= 1
        if (warp.breakCount <= 0) {
            warpRepository.remove(warp.id)
            return BreakWarpResult.Success(warp)
        }

        // Warp still needs more breaks to fully break
        return BreakWarpResult.Breaking(warp.breakCount)
    }
}