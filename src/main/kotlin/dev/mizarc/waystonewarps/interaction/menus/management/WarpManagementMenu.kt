package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.utils.lore
import dev.mizarc.waystonewarps.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class WarpManagementMenu(private val menuNavigator: MenuNavigator, private val warp: Warp): Menu {
    override fun open(player: Player) {
        val gui = ChestGui(1, "Warp '${warp.name}'")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add player count icon
        val playerCountItem = ItemStack(Material.PLAYER_HEAD)
            .name("Player Count:")
            .lore("${warpAccessRepository.getByWarp(warp).count()}")
        val guiPlayerCountItem = GuiItem(playerCountItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiPlayerCountItem, 0, 0)

        // Add icon editor button
        val iconEditorItem = ItemStack(Material.valueOf(warp.icon))
            .name("Edit Warp Icon")
            .lore("Changes the icon that shows up on the warp list")
        val guiIconEditorItem = GuiItem(iconEditorItem) {
            menuNavigator.openMenu(player, WarpIconMenu(menuNavigator, warp)) }
        pane.addItem(guiIconEditorItem, 2, 0)

        // Add renaming icon
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name("Rename Warp")
            .lore("Renames this warp")
        val guiRenamingItem = GuiItem(renamingItem) { openWarpRenamingMenu(warp) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add direction icon
        val directionItem = ItemStack(Material.COMPASS)
            .name("Change Facing Direction")
            .lore("Alters the direction players face when they get teleported")
        val guiDirectionItem = GuiItem(directionItem) { openWarpDirectionMenu(warp) }
        pane.addItem(guiDirectionItem, 4, 0)

        // Add move icon
        val moveItem = ItemStack(Material.PISTON)
            .name("Move Warp")
            .lore("Place the provided item where you want to move the warp")
        val guiMoveItem = GuiItem(moveItem) { givePlayerMoveTool(warpBuilder.player, warp) }
        pane.addItem(guiMoveItem, 7, 0)

        // Add warp delete icon
        val deleteItem = ItemStack(Material.REDSTONE)
            .name("Delete Warp")
        val guiDeleteItem = GuiItem(deleteItem) { openWarpDeleteMenu(warp) }
        pane.addItem(guiDeleteItem, 8, 0)

        gui.show(player)
    }
}