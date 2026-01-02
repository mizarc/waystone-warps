package dev.mizarc.waystonewarps.application.actions.administration

import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.world.WorldService

class ListInvalidWarps(private val warpRepository: WarpRepository, private val worldService: WorldService) {
    /**
     * Lists all warps that are in invalid worlds.
     *
     * @return A list of warps that are in invalid worlds.
     */
    fun listAllInvalidWarps(): List<Warp> {
        return warpRepository.getAll()
            .filter { worldService.isWorldInvalid(it.worldId) }
    }
}