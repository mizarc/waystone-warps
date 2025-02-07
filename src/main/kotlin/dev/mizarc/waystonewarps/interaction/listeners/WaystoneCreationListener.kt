package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.management.WarpNamingMenu
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class WaystoneCreationListener: Listener {
    @EventHandler
    fun onLodestoneInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        val clickedBlock: Block? = event.clickedBlock

        if (event.action == Action.RIGHT_CLICK_BLOCK && clickedBlock?.type == Material.LODESTONE) {
            val blockBelow: Block = clickedBlock.getRelative(BlockFace.DOWN)
            if (blockBelow.type == Material.SMOOTH_STONE) {
                val menuNavigator = MenuNavigator()
                menuNavigator.openMenu(player, WarpNamingMenu(menuNavigator, clickedBlock.location))
            }
        }
    }
}