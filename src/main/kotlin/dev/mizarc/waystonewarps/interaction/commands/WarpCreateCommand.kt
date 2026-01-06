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
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider

@CommandAlias("warpcreate")
@CommandPermission("waystonewarps.create")
@Description("Create a new warp with the Lodestone you're looking at")
class WarpCreateCommand : BaseCommand(), KoinComponent {

    private val createWarp: CreateWarp by inject()
    private val localization: LocalizationProvider by inject()

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
                localization.get(playerId, "feedback.create.not_within_range")
            )
            return
        }

        if (targetBlock.type != Material.LODESTONE) {
            player.sendMessage(
                localization.get(playerId, "feedback.create.not_lodestone")
            )
            return
        }

        if (targetBlock.getRelative(org.bukkit.block.BlockFace.DOWN).type != Material.SMOOTH_STONE) {
            player.sendMessage(
                localization.get(playerId, "feedback.create.not_smooth_stone")
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
                localization.get(playerId, "feedback.create.world_not_found")
            )
            return
        }

        val result = createWarp.execute(
            playerId = playerId,
            name = name,
            position3D = position,
            worldId = worldId,
            baseBlock = "LODESTONE"
        )

        when (result) {
            is CreateWarpResult.Success -> {
                player.sendMessage(
                    localization.get(playerId, "feedback.create.success")
                )
            }
            CreateWarpResult.LimitExceeded -> {
                player.sendMessage(
                    localization.get(playerId, "condition.naming.limit")
                )
            }
            CreateWarpResult.NameCannotBeBlank -> {
                player.sendMessage(
                    localization.get(playerId,"condition.naming.blank")
                )
            }
            CreateWarpResult.NameAlreadyExists -> {
                player.sendMessage(
                    localization.get(playerId,"condition.naming.existing")
                )
            }
        }

    }
}
