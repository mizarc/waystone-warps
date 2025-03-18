package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpSkin
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.results.UpdateWarpSkinResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaystoneBaseInteractListener: Listener, KoinComponent {
    private val getWarpAtPosition: GetWarpAtPosition by inject()
    private val updateWarpSkin: UpdateWarpSkin by inject()

    @EventHandler
    fun onLodestoneInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val clickedBlock: Block = event.clickedBlock ?: return
        val itemInHand = event.item ?: return

        // Check for right click lodestone
        if (event.action != Action.RIGHT_CLICK_BLOCK || clickedBlock.type != Material.BARRIER) return

        // Check for valid waystone above
        val blockAbove: Block = clickedBlock.getRelative(BlockFace.UP)

        // Check for existing warp
        val warp = getWarpAtPosition.execute(blockAbove.location.toPosition3D(), blockAbove.world.uid)

        // Swap out block type if block is compatible
        event.isCancelled = true
        warp?.let {
                val result = updateWarpSkin.execute(warp.id, itemInHand.type.toString())
                when (result) {
                    UpdateWarpSkinResult.SUCCESS -> player.sendActionBar(Component.text("Updated waystone skin!"))
                    UpdateWarpSkinResult.WARP_NOT_FOUND -> player.sendActionBar(Component.text("Waystone is invalid"))
                    UpdateWarpSkinResult.BLOCK_NOT_VALID -> player.sendActionBar(
                        Component.text("Item in hand cannot be used to change waystone appearance"))
                }
        }
    }
}