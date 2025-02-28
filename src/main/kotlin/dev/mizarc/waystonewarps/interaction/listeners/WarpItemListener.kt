package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class WarpItemListener: Listener {

    @EventHandler
    fun onWarpItemClick(event: PlayerInteractEvent) {
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand

        // Check if the item is a compass and the action is a right-click
        if (itemInHand.type == Material.COMPASS &&
                (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val menuNavigator = MenuNavigator(player)
            val menu = WarpMenu(event.player, menuNavigator)
            menuNavigator.openMenu(menu)
        }
    }
}