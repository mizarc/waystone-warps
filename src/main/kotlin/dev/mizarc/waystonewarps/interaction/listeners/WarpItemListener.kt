package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpItemListener(private val configService: ConfigService): Listener, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    @EventHandler
    fun onWarpItemClick(event: PlayerInteractEvent) {
        if (!configService.allowWarpsMenuViaCompass()) return

        val player = event.player
        val itemInHand = player.inventory.itemInMainHand

        // Ignore if clicking on lodestone
        if (event.clickedBlock?.type == Material.LODESTONE) {
            return
        }

        // Check if the item is a compass and the action is a right-click
        if (itemInHand.type == Material.COMPASS &&
                (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val menuNavigator = MenuNavigator(player)
            val menu = WarpMenu(event.player, menuNavigator, localizationProvider)
            menuNavigator.openMenu(menu)
        }
    }
}