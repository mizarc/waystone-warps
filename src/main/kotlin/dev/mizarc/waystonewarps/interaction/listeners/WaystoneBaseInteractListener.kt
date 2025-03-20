package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpSkin
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.results.UpdateWarpSkinResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaystoneBaseInteractListener: Listener, KoinComponent {
    private val getWarpAtPosition: GetWarpAtPosition by inject()
    private val updateWarpSkin: UpdateWarpSkin by inject()

    @EventHandler
    fun onBaseInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val clickedBlock: Block = event.clickedBlock ?: return
        val itemInHand = event.item ?: return

        // Check for right click lodestone
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        // Check if the top face of a bottom slab was clicked
        val blockData = clickedBlock.blockData as? Slab ?: return
        if (blockData.type == Slab.Type.TOP) return
        if (event.blockFace != BlockFace.UP) return

        // Check for valid waystone above
        val blockAbove: Block = clickedBlock.getRelative(BlockFace.UP)

        // Check for existing warp
        val warp = getWarpAtPosition.execute(blockAbove.location.toPosition3D(), blockAbove.world.uid) ?: return

        // Check if player owns warp
        if (warp.playerId != player.uniqueId) return

        // Swap out block type if block is compatible
        val existingBlock = Material.valueOf(warp.block)
        val result = updateWarpSkin.execute(warp.id, itemInHand.type.toString())
        when (result) {
            UpdateWarpSkinResult.SUCCESS -> {
                event.isCancelled = true
                player.sendActionBar(Component.text("Updated waystone skin!")
                    .color(TextColor.color(85, 255, 85)))
                if (player.gameMode != GameMode.CREATIVE) itemInHand.amount -= 1
                clickedBlock.world.dropItem(clickedBlock.location, ItemStack(existingBlock))
                clickedBlock.world.playSound(player.location, Sound.BLOCK_VAULT_CLOSE_SHUTTER,
                    SoundCategory.BLOCKS, 1.0f, 1.0f)
            }
            UpdateWarpSkinResult.WARP_NOT_FOUND -> player.sendActionBar(Component.text("Waystone is invalid"))
            else -> {}
        }
    }

    @EventHandler
    fun onBasePlace(event: BlockPlaceEvent) {
        val blockToPlace = event.block
        val blockData = blockToPlace.blockData as? Slab ?: return
        if (blockData.type != Slab.Type.DOUBLE) return
        if (getWarpAtPosition.execute(blockToPlace.location.add(0.0, 1.0, 0.0).toPosition3D(),
                blockToPlace.world.uid) == null) return

        // Check if the placement is attempting to place a slab above the bottom slab
        if (blockToPlace.type.toString().endsWith("_SLAB")) {
            event.isCancelled = true
        }
    }
}