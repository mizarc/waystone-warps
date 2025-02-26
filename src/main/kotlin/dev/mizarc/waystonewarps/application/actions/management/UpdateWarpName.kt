package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class UpdateWarpName(private val warpRepository: WarpRepository) {
    fun execute(warpId: UUID, playerId: UUID, name: String): UpdateWarpNameResult {
        if (name.isBlank()) return UpdateWarpNameResult.NAME_BLANK
        if (warpRepository.getByName(playerId, name) != null) {
             return UpdateWarpNameResult.NAME_ALREADY_TAKEN
        }
        val warp = warpRepository.getById(warpId) ?: return UpdateWarpNameResult.WARP_NOT_FOUND
        warp.name = name
        warpRepository.update(warp)
        return UpdateWarpNameResult.SUCCESS
    }
}