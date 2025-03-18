package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.management.ToggleLock
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.getWarpMoveTool
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.checkerframework.checker.units.qual.s
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpManagementMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                         private val warp: Warp): Menu, KoinComponent {
    private val getWarpPlayerAccess: GetWarpPlayerAccess by inject()
    private val toggleLock: ToggleLock by inject()

    override fun open() {
        val gui = ChestGui(1, "Warp '${warp.name}'")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add privacy modes
        val privacyIcon: ItemStack = if (warp.isLocked) {
            ItemStack(Material.LEVER)
                .name("§rAccess is §cPRIVATE")
                .lore("Only whitelisted players can discover and teleport.")
        } else {
            ItemStack(Material.REDSTONE_TORCH)
                .name("§rAccess is §aPUBLIC")
                .lore("All players can discover and teleport.")
        }
        val guiPrivacyItem = GuiItem(privacyIcon) {
            toggleLock.execute(warp.id)
            open()
        }
        pane.addItem(guiPrivacyItem, 0, 0)

        // Add player count icon
        val playerCountItem = ItemStack(Material.PLAYER_HEAD)
            .name("§rDiscovered Players:")
            .lore("${getWarpPlayerAccess.execute(warp.id).count() - 1}")
        val guiPlayerCountItem = GuiItem(playerCountItem) {
            menuNavigator.openMenu(WarpPlayerMenu(player, menuNavigator, warp))
        }
        pane.addItem(guiPlayerCountItem, 1, 0)

        // Add renaming icon
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name("§rRename Warp")
            .lore("Renames this warp")
        val guiRenamingItem = GuiItem(renamingItem) {
            menuNavigator.openMenu(WarpRenamingMenu(player, menuNavigator, warp)) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add icon editor button
        val iconEditorItem = ItemStack(Material.valueOf(warp.block))
            .name("§rEdit Warp Icon")
            .lore("Changes the icon that shows up on the warp list")
        val guiIconEditorItem = GuiItem(iconEditorItem) {
            menuNavigator.openMenu(WarpIconMenu(player, menuNavigator, warp)) }
        pane.addItem(guiIconEditorItem, 4, 0)

        // Add skins menu
        val skinViewItem = ItemStack(Material.valueOf(warp.block))
            .name("§rView Available Skins")
            .lore("A list of blocks you can use to re-skin the waystone")
        val guiSkinViewItem = GuiItem(skinViewItem) {
            menuNavigator.openMenu(WarpSkinsMenu(player, menuNavigator)) }
        pane.addItem(guiSkinViewItem, 5, 0)

        // Add move icon
        val moveItem = ItemStack(Material.PISTON)
            .name("§rMove Warp")
            .lore("Place the provided item where you want to move the warp")
        val guiMoveItem = GuiItem(moveItem) { givePlayerMoveTool(player) }
        pane.addItem(guiMoveItem, 8, 0)

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