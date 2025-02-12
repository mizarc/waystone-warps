package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class GetWarpAtPosition(private val warpRepository: WarpRepository) {
    fun execute(position: Position3D, worldId: UUID): Warp? {
        val warp = warpRepository.getByPosition(position, worldId) ?: return null
        return warp
    }
}