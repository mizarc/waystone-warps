package dev.mizarc.waystonewarps.application.actions.administration

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.world.WorldService

/**
 * Handles the removal of warps that are in invalid worlds.
 *
 * @property warpRepository The repository for accessing warp data.
 * @property worldService The service for world-related operations.
 */
class RemoveAllInvalidWarps(
    private val warpRepository: WarpRepository,
    private val worldService: WorldService
) {

    /**
     * Removes all warps in invalid worlds.
     *
     * @return A Pair where the first value is the number of warps removed and the second is the total number of warps checked.
     */
    fun execute(): Pair<Int, Int> {
        val allWarps = warpRepository.getAll()
        val invalidWarps = allWarps.filter { worldService.isWorldInvalid(it.worldId) }

        invalidWarps.forEach { warp ->
            warpRepository.remove(warp.id)
        }

        return Pair(invalidWarps.size, allWarps.size)
    }
}