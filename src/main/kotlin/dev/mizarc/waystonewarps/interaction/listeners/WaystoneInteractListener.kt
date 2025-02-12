package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.management.WarpManagementMenu
import dev.mizarc.waystonewarps.interaction.menus.management.WarpNamingMenu
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaystoneInteractListener: Listener, KoinComponent {
    private val getWarpAtPosition: GetWarpAtPosition by inject()

    @EventHandler
    fun onLodestoneInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        val clickedBlock: Block = event.clickedBlock ?: return

        // Check for right click lodestone
        if (event.action != Action.RIGHT_CLICK_BLOCK || clickedBlock.type != Material.LODESTONE) return

        // Check for smooth stone or barrier below lodestone
        val blockBelow: Block = clickedBlock.getRelative(BlockFace.DOWN)
        if (blockBelow.type != Material.SMOOTH_STONE && blockBelow.type != Material.BARRIER) return

        // Check for existing warp
        val warp = getWarpAtPosition.execute(clickedBlock.location.toPosition3D(), clickedBlock.world.uid)
        val menuNavigator = MenuNavigator()
        if (warp == null) {
            menuNavigator.openMenu(player, WarpNamingMenu(menuNavigator, clickedBlock.location))
        } else {
            if (warp.playerId == player.uniqueId) {
                menuNavigator.openMenu(player, WarpManagementMenu(menuNavigator, warp))
            }

        }
    }
}