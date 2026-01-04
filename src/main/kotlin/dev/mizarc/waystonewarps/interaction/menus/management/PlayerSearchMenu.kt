package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.color
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlayerSearchMenu(
    private val player: Player, 
    private val menuNavigator: MenuNavigator
): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    override fun open() {
        // Create menu
        val title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_SEARCH_TITLE)
        val gui = AnvilGui(title)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val headItem = ItemStack(Material.PLAYER_HEAD).name("")
        val guiHeadItem = GuiItem(headItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiHeadItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME),
            PrimaryColourPalette.SUCCESS.color!!
        )
        val confirmGuiItem = GuiItem(confirmItem) { _ ->
            menuNavigator.goBackWithData(gui.renameText)
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)

        gui.show(player)
    }
}