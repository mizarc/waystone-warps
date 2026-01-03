package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.world.MoveWarp
import dev.mizarc.waystonewarps.application.results.MoveWarpResult
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class MoveToolListener: Listener, KoinComponent {
    private val moveWarp: MoveWarp by inject()
    private val warpRepository: WarpRepository by inject()
    private val localizationProvider: LocalizationProvider by inject()

    @EventHandler
    fun onClaimMoveBlockPlace(event: BlockPlaceEvent) {
        // Check to see if item in hand is the warp mover
        val warpId = event.itemInHand.itemMeta.persistentDataContainer.get(
            NamespacedKey("waystonewarps","warp"), PersistentDataType.STRING) ?: return

        // Get the warp to check permissions
        val warp = warpRepository.getById(UUID.fromString(warpId))
        if (warp == null) {
            val message = localizationProvider.get(
                event.player.uniqueId, 
                LocalizationKeys.FEEDBACK_MOVE_TOOL_WARP_NOT_FOUND
            )
            event.player.sendActionBar(
                Component.text(message)
                    .color(PrimaryColourPalette.FAILED.color))
            event.isCancelled = true
            return
        }

        // Check if player has permission to relocate this warp
        if (!PermissionHelper.canRelocate(event.player, warp.playerId)) {
            val message = localizationProvider.get(
                event.player.uniqueId, 
                LocalizationKeys.FEEDBACK_MOVE_TOOL_NO_PERMISSION
            )
            event.player.sendActionBar(
                Component.text(message)
                    .color(PrimaryColourPalette.FAILED.color))
            event.isCancelled = true
            return
        }

        // Check if block above is clear
        val aboveLocation = event.block.location.clone()
        aboveLocation.add(0.0, 1.0, 0.0)
        if (event.block.world.getBlockAt(aboveLocation).type != Material.AIR) {
            val message = localizationProvider.get(
                event.player.uniqueId, 
                LocalizationKeys.FEEDBACK_MOVE_TOOL_NO_SPACE
            )
            event.player.sendActionBar(
                Component.text(message)
                    .color(PrimaryColourPalette.FAILED.color))
            event.isCancelled = true
            return
        }

        // Try to move warp
        val result = moveWarp.execute(
            event.player.uniqueId, 
            UUID.fromString(warpId), 
            aboveLocation.toPosition3D(),
            bypassOwnership = event.player.hasPermission("waystonewarps.bypass.relocate")
        )
        when (result) {
            MoveWarpResult.SUCCESS -> {
                val message = localizationProvider.get(
                    event.player.uniqueId, 
                    LocalizationKeys.FEEDBACK_MOVE_TOOL_SUCCESS
                )
                event.player.sendActionBar(
                    Component.text(message)
                        .color(PrimaryColourPalette.SUCCESS.color))
            }
            MoveWarpResult.NOT_OWNER -> {
                val message = localizationProvider.get(
                    event.player.uniqueId, 
                    LocalizationKeys.FEEDBACK_MOVE_TOOL_NOT_OWNER
                )
                event.player.sendActionBar(
                    Component.text(message)
                        .color(PrimaryColourPalette.FAILED.color))
                event.isCancelled = true
            }
            MoveWarpResult.WARP_NOT_FOUND -> {
                val message = localizationProvider.get(
                    event.player.uniqueId, 
                    LocalizationKeys.FEEDBACK_MOVE_TOOL_WARP_NOT_FOUND
                )
                event.player.sendActionBar(
                    Component.text(message)
                        .color(PrimaryColourPalette.FAILED.color))
                event.isCancelled = true
            }
        }
    }
}