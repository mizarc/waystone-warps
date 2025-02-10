package dev.mizarc.waystonewarps.application.services

/**
 * Service that handles the building of custom waystone structures.
 */
interface StructureBuilderService {
    /**
     * Builds the waystone structure in the world.
     */
    fun spawnStructure()

    /**
     * Destroys the waystone structure in the world.
     */
    fun despawnStructure()
}