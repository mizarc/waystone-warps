package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.application.services.HologramService
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class UpdateWarpName(private val warpRepository: WarpRepository, private val hologramService: HologramService) {
    fun execute(
        warpId: UUID,
        editorPlayerId: UUID,
        name: String,
        bypassOwnership: Boolean = false,
    ): UpdateWarpNameResult {
        if (name.isBlank()) return UpdateWarpNameResult.NAME_BLANK

        val warp = warpRepository.getById(warpId) ?: return UpdateWarpNameResult.WARP_NOT_FOUND

        // Check if the editor owns the warp
        if (warp.playerId != editorPlayerId && !bypassOwnership) {
            return UpdateWarpNameResult.NOT_AUTHORIZED
        }

        // Use the warp owner's ID to check for duplicate names
        if (warpRepository.getByName(warp.playerId, name) != null) {
             return UpdateWarpNameResult.NAME_ALREADY_TAKEN
        }

        warp.name = name
        warpRepository.update(warp)
        hologramService.updateHologram(warp)
        return UpdateWarpNameResult.SUCCESS
    }
}