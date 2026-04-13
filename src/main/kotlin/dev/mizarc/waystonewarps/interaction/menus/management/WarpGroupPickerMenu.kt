package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.groups.GetAllWarpGroups
import dev.mizarc.waystonewarps.application.actions.management.AssignWarpGroup
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpGroupPickerMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val warp: Warp,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val getAllWarpGroups: GetAllWarpGroups by inject()
    private val assignWarpGroup: AssignWarpGroup by inject()

    private var page = 1

    override fun open() {
        val gui = ChestGui(4, localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_PICKER_TITLE))
        gui.setOnTopClick { it.isCancelled = true }

        val controlsPane = StaticPane(0, 0, 6, 1)

        // Back button
        val backItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME), PrimaryColourPalette.CANCELLED.color!!)
        controlsPane.addItem(GuiItem(backItem) { menuNavigator.goBack() }, 0, 0)

        // No Group button
        val noGroupItem = ItemStack(Material.GRAY_CONCRETE)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_PICKER_ITEM_NONE_NAME), PrimaryColourPalette.UNAVAILABLE.color!!)
            .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_PICKER_ITEM_NONE_LORE))
        controlsPane.addItem(GuiItem(noGroupItem) {
            assignWarpGroup.execute(
                editorPlayerId = player.uniqueId,
                warpId = warp.id,
                groupId = null,
                bypassOwnership = player.hasPermission("waystonewarps.bypass.rename")
            )
            menuNavigator.goBack()
        }, 1, 0)
        gui.addPane(controlsPane)

        val groups = getAllWarpGroups.execute()

        // Build paginated group list (rows 1-3, full width, no border)
        val groupPane = PaginatedPane(0, 1, 9, 3)
        var currentPagePane = OutlinePane(0, 0, 9, 3)
        var counter = 0

        for (group in groups) {
            val isCurrent = warp.groupId == group.id
            val loreLine = if (isCurrent) {
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_PICKER_ITEM_CURRENT)
            } else {
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_PICKER_ITEM_CLICK)
            }
            val groupItem = ItemStack(if (isCurrent) Material.BOOKSHELF else Material.CHISELED_BOOKSHELF)
                .name(group.name)
                .lore(loreLine)
            currentPagePane.addItem(GuiItem(groupItem) {
                assignWarpGroup.execute(
                    editorPlayerId = player.uniqueId,
                    warpId = warp.id,
                    groupId = group.id,
                    bypassOwnership = player.hasPermission("waystonewarps.bypass.rename")
                )
                menuNavigator.goBack()
            })
            counter++
            if (counter >= 27) {
                groupPane.addPage(currentPagePane)
                currentPagePane = OutlinePane(0, 0, 9, 3)
                counter = 0
            }
        }
        if (counter > 0) groupPane.addPage(currentPagePane)
        if (groups.isEmpty()) groupPane.addPage(OutlinePane(0, 0, 9, 3))
        gui.addPane(groupPane)

        // Paginator
        val totalPages = groupPane.pages.coerceAtLeast(1)
        val paginatorPane = StaticPane(6, 0, 3, 1)

        fun updatePaginator() {
            paginatorPane.clear()
            val pageItem = ItemStack(Material.PAPER)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PAGE_NAME,
                    page.toString(), totalPages.toString()), PrimaryColourPalette.INFO.color!!)
            paginatorPane.addItem(GuiItem(pageItem), 1, 0)

            if (page > 1) {
                val prevItem = ItemStack(Material.SPECTRAL_ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME))
                paginatorPane.addItem(GuiItem(prevItem) {
                    page--
                    groupPane.page = page - 1
                    updatePaginator()
                    gui.update()
                }, 0, 0)
            } else {
                paginatorPane.addItem(GuiItem(ItemStack(Material.ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME), PrimaryColourPalette.UNAVAILABLE.color)), 0, 0)
            }

            if (page < totalPages) {
                val nextItem = ItemStack(Material.SPECTRAL_ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME))
                paginatorPane.addItem(GuiItem(nextItem) {
                    page++
                    groupPane.page = page - 1
                    updatePaginator()
                    gui.update()
                }, 2, 0)
            } else {
                paginatorPane.addItem(GuiItem(ItemStack(Material.ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME), PrimaryColourPalette.UNAVAILABLE.color)), 2, 0)
            }
        }

        updatePaginator()
        gui.addPane(paginatorPane)
        gui.show(player)
    }
}
