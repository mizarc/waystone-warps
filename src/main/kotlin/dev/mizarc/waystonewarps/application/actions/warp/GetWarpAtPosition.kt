package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class GetWarpAtPosition(private val warpRepository: WarpRepository) {
    fun execute(position: Position3D, worldId: UUID): Warp? {
        return warpRepository.getByPosition(position, worldId)
    }
}