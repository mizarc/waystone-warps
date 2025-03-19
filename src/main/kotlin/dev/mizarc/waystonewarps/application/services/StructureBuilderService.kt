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
     * Refreshes the structure with new warp metadata.
     */
    fun updateStructure(warp: Warp)

    /**
     * Reverts the structure back into its base components
     */
    fun revertStructure(warp: Warp)

    /**
     * Destroys the waystone structure in the world.
     */
    fun destroyStructure(warp: Warp)
}