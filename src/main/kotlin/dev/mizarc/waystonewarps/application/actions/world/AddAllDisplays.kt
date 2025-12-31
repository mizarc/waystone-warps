package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.application.services.HologramService
import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.warps.WarpRepository

class AddAllDisplays(private val warpRepository: WarpRepository,
                         private val structureBuilderService: StructureBuilderService,
                         private val hologramService: HologramService) {
    fun execute() {
        val warps = warpRepository.getAll()
        for (warp in warps) {
            structureBuilderService.spawnStructure(warp)
            hologramService.spawnHologram(warp)
        }
    }
}