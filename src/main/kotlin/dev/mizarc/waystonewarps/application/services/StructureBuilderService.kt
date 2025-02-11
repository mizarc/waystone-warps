package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp

/**
 * Service that handles the building of custom waystone structures.
 */
interface StructureBuilderService {
    /**
     * Builds the waystone structure in the world.
     */
    fun spawnStructure(warp: Warp)

    /**
     * Destroys the waystone structure in the world.
     */
    fun despawnStructure(warp: Warp)
}