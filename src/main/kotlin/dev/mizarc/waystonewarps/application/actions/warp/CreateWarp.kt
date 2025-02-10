package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

/**
 * This action handles the validation and business logic for the creation
 * of warps.
 *
 * @property warpRepository Repository for accessing warp data.
 * @property playerAttributeService Service for accessing player attributes.
 */
class CreateWarp(private val warpRepository: WarpRepository,
                 private val playerAttributeService: PlayerAttributeService,
                 private val structureBuilderService: StructureBuilderService) {

    /**
     * Executes the warp creation action.
     *
     * This method checks the provided name for validity, ensures the player
     * does not exceed their warp limit, and creates the warp if all conditions
     * are met.
     *
     * @param playerId The ID of the player creating the warp.
     * @param name The name of the warp to be created.
     * @return A `CreateWarpResult` indicating the outcome of the operation.
     */
    fun execute(playerId: UUID, name: String, position3D: Position3D, worldId: UUID): CreateWarpResult {
        val warps = warpRepository.getByPlayer(playerId)
        if (warps.count() >= playerAttributeService.getWarpLimit(playerId)) {
            return CreateWarpResult.LimitExceeded
        }

        if (name.isBlank()) {
            return CreateWarpResult.NameCannotBeBlank
        }

        val existingWarp = warpRepository.getByName(playerId, name)
        if (existingWarp != null) {
            return CreateWarpResult.NameAlreadyExists
        }

        val newWarp = Warp(worldId, playerId, position3D, name)
        warpRepository.add(newWarp)
        structureBuilderService.spawnStructure(newWarp)
        return CreateWarpResult.Success(newWarp)
    }
}