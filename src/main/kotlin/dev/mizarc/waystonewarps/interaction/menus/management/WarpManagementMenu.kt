package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.warp.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.getWarpMoveTool
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpManagementMenu(private val menuNavigator: MenuNavigator, private val warp: Warp): Menu, KoinComponent {
    private val getWarpPlayerAccess: GetWarpPlayerAccess by inject()

    override fun open(player: Player) {
        val gui = ChestGui(1, "Warp '${warp.name}'")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add player count icon
        val playerCountItem = ItemStack(Material.PLAYER_HEAD)
            .name("Player Count:")
            .lore("${getWarpPlayerAccess.execute(warp.id).count()}")
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
        val guiRenamingItem = GuiItem(renamingItem) {
            menuNavigator.openMenu(player, WarpRenamingMenu(menuNavigator, warp)) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add move icon
        val moveItem = ItemStack(Material.PISTON)
            .name("Move Warp")
            .lore("Place the provided item where you want to move the warp")
        val guiMoveItem = GuiItem(moveItem) { givePlayerMoveTool(player) }
        pane.addItem(guiMoveItem, 7, 0)

        gui.show(player)
    }

    private fun givePlayerMoveTool(player: Player) {
        for (item in player.inventory.contents) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getWarpMoveTool(warp).itemMeta) {
                return
            }
        }
        player.inventory.addItem(getWarpMoveTool(warp))
    }
}