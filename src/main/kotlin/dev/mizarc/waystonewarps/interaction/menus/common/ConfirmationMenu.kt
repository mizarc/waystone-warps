package dev.mizarc.waystonewarps.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfirmationMenu(
    private val menuNavigator: MenuNavigator, 
    private val player: Player,
    private val title: String, 
    private val callbackAction: () -> Unit
): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    override fun open() {
        // Create menu
        val gui = HopperGui(title)
        val pane = StaticPane(1, 0, 3, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.slotsComponent.addPane(pane)

        // Add no menu item
        val noItem = ItemStack(Material.RED_CONCRETE)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_CONFIRMATION_ITEM_NO_NAME), PrimaryColourPalette.CANCELLED.color!!)
            .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_CONFIRMATION_ITEM_NO_LORE))

        val guiNoItem = GuiItem(noItem) { guiEvent ->
            menuNavigator.goBack()
        }
        pane.addItem(guiNoItem, 0, 0)

        // Add yes menu item
        val yesItem = ItemStack(Material.GREEN_CONCRETE)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_CONFIRMATION_ITEM_YES_NAME), PrimaryColourPalette.SUCCESS.color!!)
            .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_CONFIRMATION_ITEM_YES_LORE))
        val guiYesItem = GuiItem(yesItem) { guiEvent ->
            callbackAction()
            menuNavigator.goBack()
        }
        pane.addItem(guiYesItem, 2, 0)

        gui.show(player)
    }
}