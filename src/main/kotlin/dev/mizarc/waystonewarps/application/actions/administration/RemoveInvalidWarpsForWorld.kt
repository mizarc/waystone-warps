package dev.mizarc.waystonewarps.application.actions.administration

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.domain.world.WorldService
import java.util.UUID

/**
 * Handles the removal of warps in invalid worlds for a specific world.
 *
 * @property warpRepository The repository for accessing warp data.
 * @property worldService The service for world-related operations.
 * @property discoveryRepository The repository for managing warp discoveries.
 * @property whitelistRepository The repository for managing warp whitelists.
 */
class RemoveInvalidWarpsForWorld(
    private val warpRepository: WarpRepository,
    private val worldService: WorldService,
    private val discoveryRepository: DiscoveryRepository,
    private val whitelistRepository: WhitelistRepository
) {
    /**
     * Removes warps in invalid worlds for a specific world.
     *
     * @param worldId The world to remove invalid warps from.
     * @return A Pair where the first value is the number of warps removed.
     */
    fun execute(worldId: UUID): Pair<Int, Int> {
        val allWarps = warpRepository.getByWorld(worldId)
        val invalidWarps = allWarps.filter { worldService.isWorldInvalid(it.worldId) }

        invalidWarps.forEach { warp ->
            // Remove all discoveries for this warp
            discoveryRepository.getByWarp(warp.id).forEach { discovery ->
                discoveryRepository.remove(discovery.warpId, discovery.playerId)
            }
            
            // Remove all whitelist entries for this warp
            whitelistRepository.removeByWarp(warp.id)
            
            // Remove the warp itself
            warpRepository.remove(warp.id)
        }

        return Pair(invalidWarps.size, allWarps.size)
    }
}