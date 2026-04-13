package dev.mizarc.waystonewarps.interaction.menus.use

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import dev.mizarc.waystonewarps.application.actions.discovery.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.groups.GetAllWarpGroups
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

class WarpGroupBrowseMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val getAllWarpGroups: GetAllWarpGroups by inject()
    private val getPlayerWarpAccess: GetPlayerWarpAccess by inject()

    private var page = 1

    override fun open() {
        val gui = ChestGui(6, localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUPS_TITLE))
        gui.setOnTopClick { it.isCancelled = true }

        // Border
        val outlinePane = OutlinePane(0, 1, 9, 5)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        outlinePane.applyMask(Mask(
            "111111111",
            "100000001",
            "100000001",
            "100000001",
            "111111111"
        ))
        outlinePane.addItem(GuiItem(dividerItem) { it.isCancelled = true })
        outlinePane.setRepeat(true)
        gui.addPane(outlinePane)

        // Back button
        val controlsPane = StaticPane(0, 0, 6, 1)
        val backItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME), PrimaryColourPalette.CANCELLED.color!!)
        controlsPane.addItem(GuiItem(backItem) { menuNavigator.goBack() }, 0, 0)
        gui.addPane(controlsPane)

        // Count accessible warps per group
        val accessibleWarps = getPlayerWarpAccess.execute(player.uniqueId)
        val warpCountByGroup = accessibleWarps
            .filter { it.groupId != null }
            .groupBy { it.groupId!! }
            .mapValues { it.value.size }

        val groups = getAllWarpGroups.execute()

        // Build paginated group list
        val groupPane = PaginatedPane(1, 2, 7, 3)
        var currentPagePane = OutlinePane(0, 0, 7, 3)
        var counter = 0

        for (group in groups) {
            val count = warpCountByGroup[group.id] ?: 0
            val groupItem = ItemStack(Material.BOOKSHELF)
                .name(group.name)
                .lore(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUPS_ITEM_WARP_COUNT, count.toString()),
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUPS_ITEM_CLICK_TO_BROWSE)
                )
            currentPagePane.addItem(GuiItem(groupItem) {
                menuNavigator.openMenu(WarpMenu(player, menuNavigator, localizationProvider, group.id, group.name))
            })
            counter++
            if (counter >= 21) {
                groupPane.addPage(currentPagePane)
                currentPagePane = OutlinePane(0, 0, 7, 3)
                counter = 0
            }
        }
        if (counter > 0) groupPane.addPage(currentPagePane)
        if (groups.isEmpty()) groupPane.addPage(OutlinePane(0, 0, 7, 3))
        gui.addPane(groupPane)

        addPaginator(gui, groupPane.pages.coerceAtLeast(1), page) { newPage ->
            page = newPage
            groupPane.page = page - 1
        }

        gui.show(player)
    }

    private fun addPaginator(gui: ChestGui, totalPages: Int, page: Int, updateContent: (Int) -> Unit) {
        var currentPage = page
        val paginatorPane = StaticPane(6, 0, 3, 1)

        fun updatePaginator() {
            paginatorPane.clear()

            val pageNumberItem = ItemStack(Material.PAPER)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PAGE_NAME,
                    currentPage.toString(), totalPages.toString()), PrimaryColourPalette.INFO.color!!)
            paginatorPane.addItem(GuiItem(pageNumberItem), 1, 0)

            if (currentPage <= 1) {
                val prevItem = ItemStack(Material.ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME), PrimaryColourPalette.UNAVAILABLE.color!!)
                paginatorPane.addItem(GuiItem(prevItem), 0, 0)
            } else {
                val prevItem = ItemStack(Material.SPECTRAL_ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME))
                paginatorPane.addItem(GuiItem(prevItem) {
                    currentPage--
                    updateContent(currentPage)
                    updatePaginator()
                    gui.update()
                }, 0, 0)
            }

            if (currentPage >= totalPages) {
                val nextItem = ItemStack(Material.ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME), PrimaryColourPalette.UNAVAILABLE.color!!)
                paginatorPane.addItem(GuiItem(nextItem), 2, 0)
            } else {
                val nextItem = ItemStack(Material.SPECTRAL_ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME))
                paginatorPane.addItem(GuiItem(nextItem) {
                    currentPage++
                    updateContent(currentPage)
                    updatePaginator()
                    gui.update()
                }, 2, 0)
            }
        }

        updatePaginator()
        gui.addPane(paginatorPane)
    }
}
