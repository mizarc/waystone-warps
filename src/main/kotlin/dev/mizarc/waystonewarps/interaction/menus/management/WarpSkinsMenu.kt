package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.management.GetAllWarpSkins
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpSkinsMenu(private val player: Player, private val menuNavigator: MenuNavigator): Menu, KoinComponent {
    private val getAllWarpSkins: GetAllWarpSkins by inject()

    override fun open() {
        // Create menu
        val gui = ChestGui(3, "Available Skins")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add divider pane
        val dividerPane = StaticPane(1, 0, 1, 3)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
        for (i in 0..2) {
            dividerPane.addItem(guiDividerItem, 0, i)
        }
        gui.addPane(dividerPane)

        // Add back menu item
        val navigationPane = StaticPane(0, 0, 1, 3)
        gui.addPane(navigationPane)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Back")
        val confirmGuiItem = GuiItem(confirmItem) { menuNavigator.goBack() }
        navigationPane.addItem(confirmGuiItem, 0, 0)

        // Add tooltip menu item
        val tooltipItem = ItemStack(Material.PAPER)
            .name("Blocks here are only for display")
            .lore("If you own the block, hold the item in your hand and")
            .lore("right click the base of the waystone to change the skin.")
        val tooltipGuiItem = GuiItem(tooltipItem) { menuNavigator.goBack() }
        navigationPane.addItem(tooltipGuiItem, 0, 2)

        // Display list of blocks
        displayBlockList(gui)

        gui.show(player)
    }

    private fun displayBlockList(gui: ChestGui) {
        val blocks = getAllWarpSkins.execute().map { Material.valueOf(it) }

        val blockListPane = OutlinePane(2, 0, 7, 3)
        for (block in blocks) {
            val blockItem = ItemStack(block)
            val blockGuiItem = GuiItem(blockItem) { guiEvent -> guiEvent.isCancelled = true }
            blockListPane.addItem(blockGuiItem)
        }
        gui.addPane(blockListPane)
    }
}