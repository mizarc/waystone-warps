package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.application.results.MoveWarpResult
import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class MoveWarp(private val warpRepository: WarpRepository,
               private val structureBuilderService: StructureBuilderService) {
    fun execute(playerId: UUID, warpId: UUID, position: Position3D): MoveWarpResult {
        val warp = warpRepository.getById(warpId) ?: return MoveWarpResult.WARP_NOT_FOUND

        if (warp.playerId != playerId) {
            return MoveWarpResult.NOT_OWNER
        }

        structureBuilderService.destroyStructure(warp)
        warp.position = position
        structureBuilderService.spawnStructure(warp)
        warpRepository.update(warp)
        return MoveWarpResult.SUCCESS
    }
}