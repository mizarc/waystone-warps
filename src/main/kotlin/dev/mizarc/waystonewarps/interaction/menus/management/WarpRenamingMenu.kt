package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpRenamingMenu(
    private val player: Player, 
    private val menuNavigator: MenuNavigator,
    private val warp: Warp,
    private val localizationProvider: LocalizationProvider
): Menu, KoinComponent {
    private val updateWarpName: UpdateWarpName by inject()

    private var name = ""
    private var isConfirming = false

    override fun open() {
        // Check if the player has permission to rename this warp
        val canRename = PermissionHelper.canRename(player, warp.playerId)
        if (!canRename) {
            player.sendMessage("§c${localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)}")
            menuNavigator.goBack()
            return
        }

        // Create renaming menu
        val gui = AnvilGui(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_RENAMING_TITLE))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnNameInputChanged { newName ->
            if (!isConfirming) {
                name = newName
            } else {
                isConfirming = false
            }
        }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name(warp.name)
            .lore("${warp.position.x}, ${warp.position.y}, ${warp.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        val secondPane = StaticPane(0, 0, 1, 1)
        gui.secondItemComponent.addPane(secondPane)

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME), PrimaryColourPalette.SUCCESS.color!!)
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (name == warp.name) {
                menuNavigator.goBack()
                return@GuiItem
            }

            // Attempt renaming
            val result = updateWarpName.execute(
                warpId = warp.id,
                editorPlayerId = player.uniqueId,
                name = name,
                bypassOwnership = player.hasPermission("waystonewarps.bypass.rename"),
            )
            when (result) {
                UpdateWarpNameResult.SUCCESS -> menuNavigator.goBack()
                UpdateWarpNameResult.WARP_NOT_FOUND -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name(localizationProvider.get(
                            player.uniqueId, 
                            LocalizationKeys.CONDITION_NAMING_NOT_FOUND
                        ), PrimaryColourPalette.FAILED.color!!)
                    val guiPaperItem = GuiItem(paperItem)
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(name)
                    isConfirming = true
                    gui.update()
                }
                UpdateWarpNameResult.NAME_ALREADY_TAKEN -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name(localizationProvider.get(
                            player.uniqueId, 
                            LocalizationKeys.CONDITION_NAMING_EXISTING,
                            name
                        ), PrimaryColourPalette.FAILED.color!!)
                    val guiPaperItem = GuiItem(paperItem) {guiEvent ->
                        secondPane.removeItem(0, 0)
                        lodestoneItem.name(name)
                        isConfirming = true
                        gui.update()
                    }
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(name)
                    isConfirming = true
                    gui.update()
                }
                UpdateWarpNameResult.NAME_BLANK -> menuNavigator.goBack()
                UpdateWarpNameResult.NOT_AUTHORIZED -> {
                    player.sendMessage("§c${localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_NO_PERMISSION)}")
                    menuNavigator.goBack()
                }
            }
        }

        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}