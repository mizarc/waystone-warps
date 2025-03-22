package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class GetOwnedWarps(private val warpRepository: WarpRepository) {

    fun execute(playerId: UUID): List<Warp> {
        return warpRepository.getByPlayer(playerId)
    }
}