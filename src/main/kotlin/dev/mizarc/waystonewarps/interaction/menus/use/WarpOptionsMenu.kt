package dev.mizarc.waystonewarps.interaction.menus.use

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.discovery.IsPlayerFavouriteWarp
import dev.mizarc.waystonewarps.application.actions.discovery.RevokeDiscovery
import dev.mizarc.waystonewarps.application.actions.discovery.ToggleFavouriteDiscovery
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.ConfirmationMenu
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
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

class WarpOptionsMenu(
    private val player: Player, 
    private val menuNavigator: MenuNavigator, 
    private val warp: Warp,
    private val localizationProvider: LocalizationProvider
): Menu, KoinComponent {
    private val revokeDiscovery: RevokeDiscovery by inject()
    private val isPlayerFavouriteWarp: IsPlayerFavouriteWarp by inject()
    private val toggleFavouriteDiscovery: ToggleFavouriteDiscovery by inject()

    override fun open() {
        // Create menu
        val gui = HopperGui(localizationProvider.get(
            player.uniqueId, 
            LocalizationKeys.MENU_WARP_OPTIONS_TITLE, 
            warp.name
        ))
        val pane = StaticPane(0, 0, 5, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.slotsComponent.addPane(pane)

        // Add back menu item
        val backItem = ItemStack(Material.NETHER_STAR).name(
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME), PrimaryColourPalette.CANCELLED.color!!
        )
        val guiBackItem = GuiItem(backItem) { guiEvent ->
            menuNavigator.goBack()
        }
        pane.addItem(guiBackItem, 0, 0)

        // Add point menu item
        val guiPointItem: GuiItem
        if (player.inventory.itemInMainHand.type == Material.COMPASS) {
            val pointItem = ItemStack(Material.COMPASS)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_LOCATE_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_LOCATE_LORE))
            guiPointItem = GuiItem(pointItem) { guiEvent ->
                givePlayerLodestoneCompass()
            }
            pane.addItem(guiPointItem, 2, 0)
        }
        else {
            val pointItem = ItemStack(Material.WIND_CHARGE)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_NO_COMPASS_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_NO_COMPASS_LORE))
            guiPointItem = GuiItem(pointItem)
            pane.addItem(guiPointItem, 2, 0)
        }

        // Add favourite menu item
        val favouriteItem = if (isPlayerFavouriteWarp.execute(player.uniqueId, warp.id)) {
            ItemStack(Material.DIAMOND)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_UNFAVOURITE_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_UNFAVOURITE_LORE))
        } else {
            ItemStack(Material.COAL)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_FAVOURITE_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_FAVOURITE_LORE))
        }
        val guiFavouriteItem = GuiItem(favouriteItem) { guiEvent ->
            toggleFavouriteDiscovery.execute(player.uniqueId, warp.id)
            open()
        }
        pane.addItem(guiFavouriteItem, 3, 0)

        // Add delete menu item
        val guiDeleteItem: GuiItem
        if (warp.playerId == player.uniqueId) {
            val deleteItem = ItemStack(Material.SNOWBALL)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_CANNOT_DELETE_NAME), PrimaryColourPalette.UNAVAILABLE.color!!)
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_CANNOT_DELETE_LORE))
            guiDeleteItem = GuiItem(deleteItem)
        } else {
            val deleteItem = ItemStack(Material.FIRE_CHARGE)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_DELETE_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_OPTIONS_ITEM_DELETE_LORE))
            guiDeleteItem = GuiItem(deleteItem) { guiEvent ->
                val confirmMessage = localizationProvider.get(
                    player.uniqueId, 
                    LocalizationKeys.MENU_WARP_OPTIONS_CONFIRM_DELETE,
                    warp.name
                )
                menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player, confirmMessage) {
                    revokeDiscovery.execute(player.uniqueId, warp.id)
                    menuNavigator.goBack()
                })
            }
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
            meta.isLodestoneTracked = false

            compass.itemMeta = meta

            // Replace the currently held item
            player.inventory.setItemInMainHand(compass)
        }
    }
}