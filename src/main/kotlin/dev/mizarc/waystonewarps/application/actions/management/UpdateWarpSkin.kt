package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.application.results.UpdateWarpSkinResult
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class UpdateWarpSkin(private val warpRepository: WarpRepository,
                     private val structureBuilderService: StructureBuilderService,
                     private val configService: ConfigService,
                     private val warpEventPublisher: WarpEventPublisher) {

    fun execute(warpId: UUID, blockName: String): UpdateWarpSkinResult {
        val warp = warpRepository.getById(warpId) ?: return UpdateWarpSkinResult.WARP_NOT_FOUND
        if (configService.getStructureBlocks(blockName).isEmpty()) return UpdateWarpSkinResult.BLOCK_NOT_VALID
        if (blockName == warp.block) return UpdateWarpSkinResult.UNCHANGED
        val oldWarp = warp.copy()
        warp.block = blockName
        warpRepository.update(warp)
        structureBuilderService.updateStructure(warp)
        warpEventPublisher.warpModified(oldWarp, warp)
        return UpdateWarpSkinResult.SUCCESS
    }
}