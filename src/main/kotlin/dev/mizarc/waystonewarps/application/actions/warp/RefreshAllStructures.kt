package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.warps.WarpRepository

class RefreshAllStructures(private val warpRepository: WarpRepository,
                           private val structureBuilderService: StructureBuilderService) {
    fun execute() {
        val warps = warpRepository.getAll()
        for (warp in warps) {
            structureBuilderService.despawnStructure(warp)
            structureBuilderService.spawnStructure(warp)
        }
    }
}