package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.world.MoveWarp
import dev.mizarc.waystonewarps.application.results.MoveWarpResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
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

    @EventHandler
    fun onClaimMoveBlockPlace(event: BlockPlaceEvent) {
        event.isCancelled = false
        val warpId = event.itemInHand.itemMeta.persistentDataContainer.get(
            NamespacedKey("waystonewarps","warp"), PersistentDataType.STRING) ?: return

        val aboveLocation = event.block.location.clone()
        aboveLocation.add(0.0, 1.0, 0.0)
        if (event.block.world.getBlockAt(aboveLocation).type != Material.AIR) {
            event.player.sendActionBar(
                Component.text("No space to more warp here!")
                    .color(PrimaryColourPalette.FAILED.color))
            return
        }

        val result = moveWarp.execute(event.player.uniqueId, UUID.fromString(warpId),
            aboveLocation.toPosition3D())

        when (result) {
            MoveWarpResult.SUCCESS -> {
                event.player.sendActionBar(
                    Component.text("Warp position has been moved")
                        .color(PrimaryColourPalette.SUCCESS.color))
            }
            MoveWarpResult.NOT_OWNER -> {
                event.player.sendActionBar(
                    Component.text("You don't own this warp!")
                        .color(PrimaryColourPalette.FAILED.color))
            }
            MoveWarpResult.WARP_NOT_FOUND -> {
                event.player.sendActionBar(
                    Component.text("The warp you're trying to move can't be found!")
                        .color(PrimaryColourPalette.FAILED.color))
            }
        }
    }
}