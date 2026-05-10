package dev.mizarc.waystonewarps.interaction.commands

import org.bukkit.Material
import org.bukkit.entity.Player

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Name

import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.actions.world.IsValidWarpBase
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import org.bukkit.block.BlockFace

@CommandAlias("warpcreate")
@CommandPermission("waystonewarps.create")
@Description("Create a new warp with the Lodestone you're looking at")
class WarpCreateCommand : BaseCommand(), KoinComponent {

    private val createWarp: CreateWarp by inject()
    private val localization: LocalizationProvider by inject()
    private val configService: ConfigService by inject()
    private val isValidWarpBase: IsValidWarpBase by inject()

    /**
     * Handles the **warpcreate** command.
     *
     * Usage: /warpcreate <name>
     *
     * @param player     the command sender
     * @param name       the warp name
     */
    @Default
    fun onWarpCreate(
        player: Player,
        @Name("name") name: String,
    ) {
        val playerId = player.uniqueId

        val targetBlock = player.getTargetBlockExact(10)
        if (targetBlock == null) {
            player.sendMessage(
                localization.get(playerId, LocalizationKeys.FEEDBACK_CREATE_NOT_WITHIN_RANGE)
            )
            return
        }

        if (targetBlock.type != Material.LODESTONE) {
            player.sendMessage(
                localization.get(playerId, LocalizationKeys.FEEDBACK_CREATE_NOT_LODESTONE)
            )
            return
        }

        // Check all possible waystone base types
        val validBaseFound = checkWaystoneStructure(targetBlock)
        if (!validBaseFound) {
            player.sendMessage(
                localization.get(playerId, LocalizationKeys.FEEDBACK_CREATE_NOT_SMOOTH_STONE)
            )
            return
        }

        val blockLoc = targetBlock.location
        val position = Position3D(
            x = blockLoc.x.toInt(),
            y = blockLoc.y.toInt(),
            z = blockLoc.z.toInt(),
        )
        val worldId = blockLoc.world?.uid ?: run {
            player.sendMessage(
                localization.get(playerId, LocalizationKeys.FEEDBACK_CREATE_WORLD_NOT_FOUND)
            )
            return
        }

        // Determine the base block type from the structure
        val baseBlockType = determineBaseBlockType(targetBlock)
        
        val result = createWarp.execute(
            playerId = playerId,
            name = name,
            position3D = position,
            worldId = worldId,
            baseBlock = baseBlockType
        )

        when (result) {
            is CreateWarpResult.Success -> {
                player.sendMessage(
                    localization.get(playerId, LocalizationKeys.FEEDBACK_CREATE_SUCCESS)
                )
            }
            CreateWarpResult.LimitExceeded -> {
                player.sendMessage(
                    localization.get(playerId, LocalizationKeys.CONDITION_NAMING_LIMIT)
                )
            }
            CreateWarpResult.NameCannotBeBlank -> {
                player.sendMessage(
                    localization.get(playerId, LocalizationKeys.CONDITION_NAMING_BLANK)
                )
            }
            CreateWarpResult.NameAlreadyExists -> {
                player.sendMessage(
                    localization.get(playerId, LocalizationKeys.CONDITION_NAMING_EXISTING)
                )
            }
        }

    }

    /**
     * Checks if the target lodestone has a valid waystone structure beneath it
     */
    private fun checkWaystoneStructure(lodestone: org.bukkit.block.Block): Boolean {
        val allSkinTypes = configService.getAllSkinTypes()
        
        for (skinType in allSkinTypes) {
            val structureBlocks = configService.getStructureBlocks(skinType)
            if (structureBlocks.size >= 3) {
                // Check the block directly below the lodestone (index 2 in structure)
                val lowerBlockType = structureBlocks[2]
                try {
                    val lowerMaterial = Material.valueOf(lowerBlockType)
                    if (lodestone.getRelative(BlockFace.DOWN).type == lowerMaterial) {
                        return true
                    }
                } catch (e: IllegalArgumentException) {
                    // Skip invalid material names
                    continue
                }
            }
        }
        return false
    }

    /**
     * Determines the base block type based on the waystone structure
     */
    private fun determineBaseBlockType(lodestone: org.bukkit.block.Block): String {
        val allSkinTypes = configService.getAllSkinTypes()
        
        for (skinType in allSkinTypes) {
            val structureBlocks = configService.getStructureBlocks(skinType)
            if (structureBlocks.size >= 3) {
                // Check the block directly below the lodestone (index 2 in structure)
                val lowerBlockType = structureBlocks[2]
                try {
                    val lowerMaterial = Material.valueOf(lowerBlockType)
                    if (lodestone.getRelative(BlockFace.DOWN).type == lowerMaterial) {
                        return skinType
                    }
                } catch (e: IllegalArgumentException) {
                    // Skip invalid material names
                    continue
                }
            }
        }
        return "SMOOTH_STONE" // Fallback to default
    }
}
