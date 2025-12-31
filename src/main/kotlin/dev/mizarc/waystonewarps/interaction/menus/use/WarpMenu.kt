package dev.mizarc.waystonewarps.interaction.menus.use

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import dev.mizarc.waystonewarps.application.actions.discovery.GetFavouritedWarpAccess
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayer
import dev.mizarc.waystonewarps.application.actions.discovery.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.management.GetOwnedWarps
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.AccentColourPalette
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.models.toViewModel
import dev.mizarc.waystonewarps.interaction.utils.applyIconMeta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import me.xdrop.fuzzywuzzy.FuzzySearch
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpMenu(private val player: Player, private val menuNavigator: MenuNavigator): Menu, KoinComponent {
    private val getPlayerWarpAccess: GetPlayerWarpAccess by inject()
    private val teleportPlayer: TeleportPlayer by inject()
    private val getWhitelistedPlayers: GetWhitelistedPlayers by inject()
    private val getFavouritedWarpAccess: GetFavouritedWarpAccess by inject()
    private val getOwnedWarps: GetOwnedWarps by inject()

    private var viewMode = 0  // 0 = All, Favourites, Owned
    private var page = 1
    private var warpNameSearch: String = ""

    override fun open() {
        val gui = ChestGui(6, "Warps")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add controls pane
        addControlsSection(gui)

        // Switch what warps to display depending on view mode
        val warps = when (viewMode) {
            0 -> getPlayerWarpAccess.execute(player.uniqueId)
            1 -> getFavouritedWarpAccess.execute(player.uniqueId)
            2 -> getOwnedWarps.execute(player.uniqueId)
            else -> emptyList()
        }.sortedBy { it.name }

        // Filter by warp name if specified
        val filteredWarps = if (warpNameSearch.isNotBlank()) {
            val playerNames = warps.map { it.name }
            val searchResults = FuzzySearch.extractAll(warpNameSearch, playerNames)
                .filter { it.score >= 60 }
                .take(21)
            searchResults.mapNotNull { result -> warps.find { it.name == result.string } }
        } else {
            warps
        }

        // Display warps
        val warpPane = displayWarps(filteredWarps)
        gui.addPane(warpPane)

        // Add warp paginator
        addPaginator(gui, warpPane.pages.coerceAtLeast(1), page) { newPage ->
            page = newPage
            warpPane.page = page - 1 // Subtract 1 since pages start at 0
        }

        gui.show(player)
    }

    override fun passData(data: Any?) {
        if (data is String) {
            warpNameSearch = data
        }
    }

    private fun addControlsSection(gui: ChestGui): StaticPane {
        // Add outline
        val outlinePane = OutlinePane(0, 1, 9, 5)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
        outlinePane.applyMask(Mask(
            "111111111",
            "100000001",
            "100000001",
            "100000001",
            "111111111"
        ))
        outlinePane.addItem(guiDividerItem)
        outlinePane.setRepeat(true)
        gui.addPane(outlinePane)

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 6, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR).name("Exit")
        val guiExitItem = GuiItem(exitItem) { menuNavigator.goBack() }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add view mode item
        val viewModeItem = when (viewMode) {
            0 -> ItemStack(Material.SUGAR)
                .name("Discovered")
                .lore("Listing all of your discovered warps")
            1 -> ItemStack(Material.GLOWSTONE_DUST)
                .name("Favourited")
                .lore("Listing only favourited warps")
            else -> ItemStack(Material.REDSTONE)
                .name("Owned")
                .lore("Listing only warps that you own")
        }
        val guiViewModeItem = GuiItem(viewModeItem) { guiEvent ->
            // Cycle through 0, 1, 2
            viewMode = when (guiEvent.isLeftClick) {
                true -> (viewMode + 1) % 3 // Cycle forwards on left click
                false -> (viewMode - 1 + 3) % 3 // Cycle backwards on right click
            }
            page = 1
            open()
        }
        controlsPane.addItem(guiViewModeItem, 2, 0)

        // Add search button
        val searchItem = ItemStack(Material.NAME_TAG).name("Search")
        val guiSearchItem = GuiItem(searchItem) {
            val warpSearchMenu = WarpSearchMenu(player, menuNavigator)
            menuNavigator.openMenu(warpSearchMenu)
        }
        controlsPane.addItem(guiSearchItem, 3, 0)

        // Add clear search button
        if (warpNameSearch.isNotEmpty()) {
            val clearSearchItem = ItemStack(Material.MAGMA_CREAM).name("Clear Search")
            val guiClearSearchItem = GuiItem(clearSearchItem) {
                warpNameSearch = ""
                page = 1
                open()
            }
            controlsPane.addItem(guiClearSearchItem, 4, 0)
        }

        return controlsPane
    }

    private fun addPaginator(gui: ChestGui, totalPages: Int, page: Int, updateContent: (Int) -> Unit) {
        var currentPage = page // Make currentPage mutable
        val paginatorPane = StaticPane(6, 0, 3, 1)

        fun updatePaginator() {
            paginatorPane.clear()

            // Update page number item
            val pageNumberItem = ItemStack(Material.PAPER).name("Page $currentPage of $totalPages")
            val guiPageNumberItem = GuiItem(pageNumberItem)
            // Clear previous page number
            paginatorPane.addItem(guiPageNumberItem, 1, 0)

            // Update left arrow
            val prevItem: ItemStack
            val guiPrevItem: GuiItem
            if (currentPage <= 1) {
                prevItem = ItemStack(Material.ARROW).name("Prev")
                guiPrevItem = GuiItem(prevItem)
            } else {
                prevItem = ItemStack(Material.SPECTRAL_ARROW).name("Prev")
                guiPrevItem = GuiItem(prevItem) {
                    currentPage--
                    updateContent(currentPage)
                    updatePaginator()
                    gui.update()
                }
            }
            paginatorPane.addItem(guiPrevItem, 0, 0)

            // Update right arrow
            val nextItem: ItemStack
            val guiNextItem: GuiItem
            if (currentPage >= totalPages) {
                nextItem = ItemStack(Material.ARROW).name("Next")
                guiNextItem = GuiItem(nextItem)
            } else {
                nextItem = ItemStack(Material.SPECTRAL_ARROW).name("Next")
                guiNextItem = GuiItem(nextItem) {
                    currentPage++
                    updateContent(currentPage)
                    updatePaginator()
                    gui.update()
                }
            }
            paginatorPane.addItem(guiNextItem, 2, 0)

            gui.update()
        }

        updatePaginator()
        gui.addPane(paginatorPane)
    }

    private fun displayWarps(warps: List<Warp>): PaginatedPane {
        val playerPane = PaginatedPane(1, 2, 7, 3)
        var currentPagePane = OutlinePane(0, 0, 7, 3)
        var playerCounter = 0
        val stockLore = listOf("Right Click for additional options")

        for (warp in warps) {
            val warpModel = warp.toViewModel()
            val locationText = warpModel.location?.let { location ->
                "${location.blockX}, ${location.blockY}, ${location.blockZ}"
            } ?: run {
                "Location not found"
            }
            val customLore = stockLore.toMutableList()
            customLore.add(0, "§3$locationText")
            customLore.add(0, "§6${warpModel.player.name}")

            var guiWarpItem: GuiItem
            if (warp.isLocked && !getWhitelistedPlayers.execute(warp.id).contains(player.uniqueId)
                    && player.uniqueId != warp.playerId) {
                customLore.add(2, "§cLOCKED")
                val warpItem = ItemStack(warpModel.icon).applyIconMeta(warp.iconMeta).name(warpModel.name).lore(customLore)
                guiWarpItem = GuiItem(warpItem) { guiEvent ->
                    if (guiEvent.isRightClick) {
                        // Right click to open options
                        menuNavigator.openMenu(WarpOptionsMenu(player, menuNavigator, warp))
                    }
                }
            }
            else {
                customLore.add(2, "Left Click to teleport")
                val warpItem = ItemStack(warpModel.icon).applyIconMeta(warp.iconMeta).name(warpModel.name).lore(customLore)
                guiWarpItem = GuiItem(warpItem) {guiEvent ->

                    if (guiEvent.isRightClick) {
                        // Right click to open options
                        menuNavigator.openMenu(WarpOptionsMenu(player, menuNavigator, warp))
                    } else {
                        // Left click to teleport
                        teleportPlayer.execute(
                            player.uniqueId, warp,
                            onPending = {
                                player.sendActionBar {
                                    Component.text("Teleporting to ").color(PrimaryColourPalette.INFO.color)
                                        .append(Component.text(warp.name).color(AccentColourPalette.INFO.color))
                                        .append(
                                            Component.text("... Don't move!").color(PrimaryColourPalette.INFO.color)
                                        )
                                }
                            },
                            onSuccess = {
                                player.sendActionBar {
                                    Component.text("Welcome to ").color(PrimaryColourPalette.SUCCESS.color)
                                        .append(Component.text(warp.name).color(AccentColourPalette.SUCCESS.color))
                                        .append(Component.text("!").color(PrimaryColourPalette.SUCCESS.color))
                                }
                            },
                            onFailure = {
                                player.sendActionBar {
                                    Component.text("Failed to teleport, contact the server administrator")
                                        .color(PrimaryColourPalette.FAILED.color)
                                }
                            },
                            onInsufficientFunds = {
                                player.sendActionBar {
                                    Component.text("Insufficient funds to teleport")
                                        .color(PrimaryColourPalette.CANCELLED.color)
                                }
                            },
                            onWorldNotFound = {
                                player.sendActionBar {
                                    Component.text("Cannot teleport to a world that does not exist")
                                        .color(PrimaryColourPalette.FAILED.color)
                                }
                            },
                            onLocked = {
                                player.sendActionBar {
                                    Component.text("Cannot teleport to a warp that is now locked")
                                        .color(PrimaryColourPalette.CANCELLED.color)
                                }
                            },
                            onCanceled = {
                                player.sendActionBar {
                                    Component.text("Cancelled teleport due to movement")
                                        .color(PrimaryColourPalette.CANCELLED.color)
                                }
                            }
                        )
                        player.closeInventory()
                        guiEvent.isCancelled = true
                    }
                }
            }

            // Add player menu item
            currentPagePane.addItem(guiWarpItem)
            playerCounter++

            // Check if the current page is full (21 players)
            if (playerCounter >= 21) {
                playerPane.addPage(currentPagePane)
                currentPagePane = OutlinePane(0, 0, 7, 3)
                playerCounter = 0
            }
        }

        // Add the last page if it's not empty
        if (playerCounter > 0) {
            playerPane.addPage(currentPagePane)
        }

        return playerPane
    }
}