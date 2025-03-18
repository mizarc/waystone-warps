package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.application.results.UpdateWarpSkinResult
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class UpdateWarpSkin(private val warpRepository: WarpRepository) {
    val allowedBlockNames = setOf("SMOOTH_STONE", "STONE_BRICKS", "DEEPSLATE_TILES", "POLISHED_TUFF", "TUFF_BRICKS",
        "RESIN_BRICKS", "CUT_SANDSTONE", "CUT_RED_SANDSTONE", "NETHER_BRICKS", "POLISHED_BLACKSTONE", "SMOOTH_QUARTZ",
        "WAXED_COPPER_BLOCK", "WAXED_EXPOSED_COPPER", "WAXED_WEATHERED_COPPER", "WAXED_OXIDIZED_COPPER")

    fun execute(warpId: UUID, blockName: String): UpdateWarpSkinResult {
        val warp = warpRepository.getById(warpId) ?: return UpdateWarpSkinResult.WARP_NOT_FOUND
        if (blockName !in allowedBlockNames) return UpdateWarpSkinResult.BLOCK_NOT_VALID
        warp.block = blockName
        warpRepository.update(warp)
        return UpdateWarpSkinResult.SUCCESS
    }
}