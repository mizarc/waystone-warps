package dev.mizarc.waystonewarps.application.actions.administration

import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.world.WorldService
import java.util.UUID

/**
 * Handles the removal of warps in invalid worlds for a specific player.
 *
 * @property warpRepository The repository for accessing warp data.
 * @property worldService The service for world-related operations.
 */
class RemoveInvalidWarpsForWorld(
    private val warpRepository: WarpRepository,
    private val worldService: WorldService
) {
    /**
     * Removes warps in invalid worlds for a specific player.
     *
     * @param worldId The world to remove invalid warps from.
     * @return A Pair where the first value is the number of warps removed.
     */
    fun execute(worldId: UUID): Pair<Int, Int> {
        val allWarps = warpRepository.getByWorld(worldId)
        val invalidWarps = allWarps.filter { worldService.isWorldInvalid(it.worldId) }

        invalidWarps.forEach { warp ->
            warpRepository.remove(warp.id)
        }

        return Pair(invalidWarps.size, allWarps.size)
    }
}