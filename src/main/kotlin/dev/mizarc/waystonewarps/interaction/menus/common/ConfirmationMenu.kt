package dev.mizarc.waystonewarps.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ConfirmationMenu(val menuNavigator: MenuNavigator, val player: Player,
                       val title: String, val callbackAction: () -> Unit): Menu {
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
            .name("No")
            .lore("Take me back")

        val guiNoItem = GuiItem(noItem) { guiEvent ->
            menuNavigator.goBack()
        }
        pane.addItem(guiNoItem, 0, 0)

        // Add yes menu item
        val yesItem = ItemStack(Material.GREEN_CONCRETE)
            .name("Yes")
            .lore("Warning, This is a permanent action")
        val guiYesItem = GuiItem(yesItem) { guiEvent ->
            callbackAction()
            menuNavigator.goBack()
        }
        pane.addItem(guiYesItem, 2, 0)

        gui.show(player)
    }
}