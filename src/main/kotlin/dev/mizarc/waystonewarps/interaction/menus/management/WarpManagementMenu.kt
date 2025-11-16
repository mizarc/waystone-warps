package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.management.ToggleLock
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
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
        val canChangeAccess = PermissionHelper.canChangeAccessControl(player, warp.playerId)
        val privacyIcon: ItemStack = if (warp.isLocked) {
            val item = ItemStack(Material.LEVER)
                .name("§rAccess is §cPRIVATE")
            if (canChangeAccess) {
                item.lore("Only whitelisted players can discover and teleport.")
            } else {
                item.lore("Only whitelisted players can discover and teleport.", "§cYou don't have permission to change this")
            }
            item
        } else {
            val item = ItemStack(Material.REDSTONE_TORCH)
                .name("§rAccess is §aPUBLIC")
            if (canChangeAccess) {
                item.lore("All players can discover and teleport.")
            } else {
                item.lore("All players can discover and teleport.", "§cYou don't have permission to change this")
            }
            item
        }
        val guiPrivacyItem = GuiItem(privacyIcon) {
            if (canChangeAccess) {
                toggleLock.execute(player.uniqueId, warp.id)
                open()
            }
        }
        pane.addItem(guiPrivacyItem, 0, 0)

        // Add player count icon
        val canManageWhitelist = PermissionHelper.canManageWhitelist(player, warp.playerId)
        val playerCountItem = ItemStack(Material.PLAYER_HEAD)
            .name("§rDiscovered Players:")
        if (canManageWhitelist) {
            playerCountItem.lore("${getWarpPlayerAccess.execute(warp.id).count() - 1}")
        } else {
            playerCountItem.lore("${getWarpPlayerAccess.execute(warp.id).count() - 1}", "§cYou don't have permission to manage the whitelist")
        }
        val guiPlayerCountItem = GuiItem(playerCountItem) {
            if (canManageWhitelist) {
                menuNavigator.openMenu(WarpPlayerMenu(player, menuNavigator, warp))
            }
        }
        pane.addItem(guiPlayerCountItem, 1, 0)

        // Add renaming icon
        val canRename = PermissionHelper.canRename(player, warp.playerId)
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name("§rRename Warp")
        if (canRename) {
            renamingItem.lore("Changes the name that is displayed")
        } else {
            renamingItem.lore("Changes the name that is displayed", "§cYou don't have permission to rename this waystone")
        }
        val guiRenamingItem = GuiItem(renamingItem) {
            if (canRename) {
                menuNavigator.openMenu(WarpRenamingMenu(player, menuNavigator, warp))
            }
        }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add icon editor button
        val canChangeIcon = PermissionHelper.canChangeIcon(player, warp.playerId)
        val iconEditorItem = ItemStack(Material.valueOf(warp.icon))
            .name("§rEdit Warp Icon")
        if (canChangeIcon) {
            iconEditorItem.lore("Changes the icon that shows up on the warp list")
        } else {
            iconEditorItem.lore("Changes the icon that shows up on the warp list", "§cYou don't have permission to change this waystone's icon")
        }
        val guiIconEditorItem = GuiItem(iconEditorItem) {
            if (canChangeIcon) {
                menuNavigator.openMenu(WarpIconMenu(player, menuNavigator, warp))
            }
        }
        pane.addItem(guiIconEditorItem, 4, 0)

        // Add skins menu
        val skinViewItem = ItemStack(Material.valueOf(warp.block))
            .name("§rView Available Skins")
            .lore("A list of blocks you can use to re-skin the waystone")
        val guiSkinViewItem = GuiItem(skinViewItem) {
            menuNavigator.openMenu(WarpSkinsMenu(player, menuNavigator)) }
        pane.addItem(guiSkinViewItem, 5, 0)

        // Add move icon
        val canRelocate = PermissionHelper.canRelocate(player, warp.playerId)
        val moveItem = ItemStack(Material.PISTON)
            .name("§rMove Warp")
        if (canRelocate) {
            moveItem.lore("Place the provided item where you want to move the warp")
        } else {
            moveItem.lore("Place the provided item where you want to move the warp", "§cYou don't have permission to move this waystone")
        }
        val guiMoveItem = GuiItem(moveItem) {
            if (canRelocate) {
                givePlayerMoveTool(player)
            }
        }
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