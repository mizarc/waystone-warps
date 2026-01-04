package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.management.ToggleLock
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.applyIconMeta
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import dev.mizarc.waystonewarps.interaction.utils.getWarpMoveTool
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpManagementMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                         private val warp: Warp): Menu, KoinComponent {
    private val getWarpPlayerAccess: GetWarpPlayerAccess by inject()
    private val toggleLock: ToggleLock by inject()
    private val localizationProvider: LocalizationProvider by inject()

    override fun open() {
        val title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_TITLE, warp.name)
        val gui = ChestGui(1, title)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add privacy modes
        val canChangeAccess = PermissionHelper.canChangeAccessControl(player, warp.playerId)

        val privacyIcon: ItemStack = if (warp.isLocked) {
            val accessName = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_NAME)
            val privateStatus = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_NAME_PRIVATE)
            val accessParts = accessName.split("{0}")
            
            val privateText = Component.text()
                .append(Component.text(accessParts[0].trimEnd(), PrimaryColourPalette.PRIMARY.color!!).decoration(TextDecoration.ITALIC, false))
                .append(Component.text(" "))
                .append(Component.text(
                    privateStatus,
                    PrimaryColourPalette.CANCELLED.color!!
                ).decoration(TextDecoration.ITALIC, false))
                .append(if (accessParts.size > 1) Component.text(accessParts[1].trimStart()) else Component.empty())
                .build()

            val item = ItemStack(Material.LEVER)
                .name(privateText)
            if (canChangeAccess) {
                item.lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_LORE_PRIVATE))
            } else {
                item.lore(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_LORE_PRIVATE),
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_LORE_NO_PERM)
                )
            }
            item
        } else {
            val accessName = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_NAME)
            val publicStatus = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_NAME_PUBLIC)
            val accessParts = accessName.split("{0}")

            val publicText = Component.text()
                .append(Component.text(accessParts[0].trimEnd(), PrimaryColourPalette.PRIMARY.color!!).decoration(TextDecoration.ITALIC, false))
                .append(Component.text(" "))
                .append(Component.text(
                    publicStatus,
                    PrimaryColourPalette.SUCCESS.color!!
                ).decoration(TextDecoration.ITALIC, false))
                .append(if (accessParts.size > 1) Component.text(accessParts[1].trimStart()) else Component.empty())
                .build()

            val item = ItemStack(Material.REDSTONE_TORCH)
                .name(publicText)
            if (canChangeAccess) {
                item.lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_LORE_PUBLIC))
            } else {
                item.lore(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_LORE_PUBLIC),
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ACCESS_LORE_NO_PERM)
                )
            }
            item
        }
        val guiPrivacyItem = GuiItem(privacyIcon) {
            if (canChangeAccess) {
                toggleLock.execute(
                    playerId = player.uniqueId,
                    warpId = warp.id,
                    bypassOwnership = player.hasPermission("waystonewarps.bypass.access_control"),
                )
                open()
            }
        }
        pane.addItem(guiPrivacyItem, 0, 0)

        // Add player count icon
        val canManageWhitelist = PermissionHelper.canManageWhitelist(player, warp.playerId)
        val playerCount = getWarpPlayerAccess.execute(warp.id).count() - 1
        val playerCountItem = ItemStack(Material.PLAYER_HEAD)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_PLAYERS))
        if (canManageWhitelist) {
            playerCountItem.lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_PLAYERS_LORE, playerCount.toString()))
        } else {
            playerCountItem.lore(
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_PLAYERS_LORE, playerCount.toString()),
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)
            )
        }
        val guiPlayerCountItem = GuiItem(playerCountItem) {
            if (canManageWhitelist) {
                menuNavigator.openMenu(WarpPlayerMenu(player, menuNavigator, warp, localizationProvider))
            }
        }
        pane.addItem(guiPlayerCountItem, 1, 0)

        // Add renaming icon
        val canRename = PermissionHelper.canRename(player, warp.playerId)
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_RENAME))
        if (canRename) {
            renamingItem.lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_RENAME_LORE))
        } else {
            renamingItem.lore(
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_RENAME_LORE),
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)
            )
        }
        val guiRenamingItem = GuiItem(renamingItem) {
            if (canRename) {
                menuNavigator.openMenu(WarpRenamingMenu(player, menuNavigator, warp, localizationProvider))
            }
        }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add icon editor button
        val canChangeIcon = PermissionHelper.canChangeIcon(player, warp.playerId)
        val iconEditorItem = ItemStack(Material.valueOf(warp.icon)).applyIconMeta(warp.iconMeta)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ICON))
        if (canChangeIcon) {
            iconEditorItem.lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ICON_LORE))
        } else {
            iconEditorItem.lore(
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_ICON_LORE),
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)
            )
        }
        val guiIconEditorItem = GuiItem(iconEditorItem) {
            if (canChangeIcon) {
                menuNavigator.openMenu(WarpIconMenu(player, menuNavigator, warp))
            }
        }
        pane.addItem(guiIconEditorItem, 4, 0)

        // Add skins menu
        val skinViewItem = ItemStack(Material.valueOf(warp.block))
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_SKINS))
            .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_SKINS_LORE))
        val guiSkinViewItem = GuiItem(skinViewItem) {
            menuNavigator.openMenu(WarpSkinsMenu(player, menuNavigator, localizationProvider))
        }
        pane.addItem(guiSkinViewItem, 5, 0)

        // Add move icon
        val canRelocate = PermissionHelper.canRelocate(player, warp.playerId)
        val moveItem = ItemStack(Material.PISTON)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_MOVE))
        if (canRelocate) {
            moveItem.lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_MOVE_LORE))
        } else {
            moveItem.lore(
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_MOVE_LORE),
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)
            )
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