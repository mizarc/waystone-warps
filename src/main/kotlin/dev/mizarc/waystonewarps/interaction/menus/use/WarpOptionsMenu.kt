package dev.mizarc.waystonewarps.interaction.menus.use

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.discovery.IsPlayerFavouriteWarp
import dev.mizarc.waystonewarps.application.actions.discovery.RevokeDiscovery
import dev.mizarc.waystonewarps.application.actions.discovery.ToggleFavouriteDiscovery
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.ConfirmationMenu
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpOptionsMenu(private val player: Player, private val menuNavigator: MenuNavigator, private val warp: Warp): Menu, KoinComponent {
    private val revokeDiscovery: RevokeDiscovery by inject()
    private val isPlayerFavouriteWarp: IsPlayerFavouriteWarp by inject()
    private val toggleFavouriteDiscovery: ToggleFavouriteDiscovery by inject()

    override fun open() {
        // Create menu
        val gui = HopperGui("Warp ${warp.name}")
        val pane = StaticPane(0, 0, 5, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.slotsComponent.addPane(pane)

        // Add back menu item
        val backItem = ItemStack(Material.NETHER_STAR).name("Go Back")
        val guiBackItem = GuiItem(backItem) { guiEvent ->
            menuNavigator.goBack()
        }
        pane.addItem(guiBackItem, 0, 0)

        // Add point menu item
        val pointItem = ItemStack(Material.COMPASS)
            .name("Point to Waystone")
            .lore("Your compass will point towards this waystone")
        val guiPointItem = GuiItem(pointItem) { guiEvent ->
            givePlayerLodestoneCompass()
        }
        pane.addItem(guiPointItem, 2, 0)

        // Add favourite menu item
        val favouriteItem = if (isPlayerFavouriteWarp.execute(player.uniqueId, warp.id)) {
            ItemStack(Material.DIAMOND)
                .name("Unfavourite")
                .lore("Deprioritises placement in the menu")
        } else {
            ItemStack(Material.COAL)
                .name("Favourite")
                .lore("Prioritises placement in the menu")
        }
        val guiFavouriteItem = GuiItem(favouriteItem) { guiEvent ->
            toggleFavouriteDiscovery.execute(player.uniqueId, warp.id)
            open()
        }
        pane.addItem(guiFavouriteItem, 3, 0)

        // Add delete menu item
        // Add favourite menu item
        val deleteItem = ItemStack(Material.FIRE_CHARGE)
            .name("Delete")
            .lore("Removes your access to this warp")
        val guiDeleteItem = GuiItem(deleteItem) { guiEvent ->
            menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player, "Delete access to ${warp.name}") {
                revokeDiscovery.execute(player.uniqueId, warp.id)
                menuNavigator.goBack()
            })
        }
        pane.addItem(guiDeleteItem, 4, 0)

        gui.show(player)
    }

    private fun givePlayerLodestoneCompass() {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val heldItem = player.inventory.itemInMainHand

        if (heldItem.type == Material.COMPASS) {
            // Create the Lodestone Compass
            val compass = ItemStack(Material.COMPASS)
            val meta = compass.itemMeta as CompassMeta

            // Set lodestone details
            meta.lodestone = warp.position.toLocation(world)
            meta.isLodestoneTracked = true

            compass.itemMeta = meta

            // Replace the currently held item
            player.inventory.setItemInMainHand(compass)
        }
    }
}