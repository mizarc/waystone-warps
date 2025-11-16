package dev.mizarc.waystonewarps.interaction.menus.management

import com.destroystokyo.paper.profile.PlayerProfile
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.discovery.RevokeDiscovery
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.application.actions.whitelist.ToggleWhitelist
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.ConfirmationMenu
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import dev.mizarc.waystonewarps.interaction.utils.createHead
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class WarpPlayerMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                     private val warp: Warp): Menu, KoinComponent {
    private val getWarpPlayerAccess: GetWarpPlayerAccess by inject()
    private val getPlayerWhitelistForWarp: GetWhitelistedPlayers by inject()
    private val toggleWhitelist: ToggleWhitelist by inject()
    private val revokeDiscovery: RevokeDiscovery by inject()

    private var viewMode = 0  // 0 = Discovered, 1 = Whitelisted, 2 = All
    private var page = 1
    private var playerNameSearch: String = ""

    override fun open() {
        // Create player access menu
        val gui = ChestGui(6, "Player Access")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        addControlsSection(gui)

        // Switch what players to display depending on view mode, exclude player who owns the warp
        val players = when (viewMode) {
            0 -> getWarpPlayerAccess.execute(warp.id).map { Bukkit.getOfflinePlayer(it) }
            1 -> getPlayerWhitelistForWarp.execute(warp.id).map { Bukkit.getOfflinePlayer(it) }
            2 -> Bukkit.getOnlinePlayers().map { it as OfflinePlayer }
            else -> emptyList()
        }.filter { it.uniqueId != warp.playerId }.sortedBy { it.name }

        // Filter by player name if specified
        val filteredPlayers = if (playerNameSearch.isNotBlank()) {
            val playerNames = players.mapNotNull { it.name }
            val searchResults = FuzzySearch.extractAll(playerNameSearch, playerNames)
                .filter { it.score >= 60 }
                .take(21)
            searchResults.mapNotNull { result -> players.find { it.name == result.string } }
        } else {
            players
        }

        // Pane of players
        val playerPane = displayPlayers(filteredPlayers, warp, gui)
        gui.addPane(playerPane)

        // Add paginator pane
        addPaginator(gui, playerPane.pages.coerceAtLeast(1), page) { newPage ->
            page = newPage
            playerPane.page = page - 1 // Subtract 1 since pages start at 0
        }

        // Display to GUI
        gui.show(player)
    }

    override fun passData(data: Any?) {
        if (data is String) {
            playerNameSearch = data
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
        val exitItem = ItemStack(Material.NETHER_STAR).name("Go Back")
        val guiExitItem = GuiItem(exitItem) { menuNavigator.goBack() }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add view mode item
        val viewModeItem = when (viewMode) {
            0 -> ItemStack(Material.SUGAR)
                .name("Discovered")
                .lore("Listing players with access to this warp")
            1 -> ItemStack(Material.GLOWSTONE_DUST)
                .name("Whitelisted")
                .lore("Listing players who are whitelisted")
            else -> ItemStack(Material.REDSTONE)
                .name("Online")
                .lore("Listing all players on the server")
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
            val playerSearchMenu = PlayerSearchMenu(player, menuNavigator)
            menuNavigator.openMenu(playerSearchMenu)
        }
        controlsPane.addItem(guiSearchItem, 3, 0)

        // Add clear search button
        if (playerNameSearch.isNotEmpty()) {
            val clearSearchItem = ItemStack(Material.MAGMA_CREAM).name("Clear Search")
            val guiClearSearchItem = GuiItem(clearSearchItem) {
                playerNameSearch = ""
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

    private fun displayPlayers(players: List<OfflinePlayer>, warp: Warp, gui: Gui): PaginatedPane {
        val playerPane = PaginatedPane(1, 2, 7, 3)
        var currentPagePane = OutlinePane(0, 0, 7, 3)
        var playerCounter = 0

        val whitelisted = getPlayerWhitelistForWarp.execute(warp.id)
        val discovered = getWarpPlayerAccess.execute(warp.id)
        val canManageWhitelist = PermissionHelper.canManageWhitelist(player, warp.playerId)
        val stockLore = if (canManageWhitelist) {
            listOf("Left Click to toggle whitelist")
        } else {
            listOf("§cYou don't have permission to manage the whitelist")
        }

        for (foundPlayer in players) {
            // Modify lore text depending on if the player has discovered this warp or is whitelisted
            val customLore = stockLore.toMutableList()
            if (foundPlayer.uniqueId in discovered) {
                customLore.add(0, "§bDiscovered")
                customLore.add("Right click to revoke access",)
            }
            if (foundPlayer.uniqueId in whitelisted) {
                customLore.add(0, "§aWhitelisted")
            }

            // Create player menu item
            val playerItem = createHead(foundPlayer)
                .name("${foundPlayer.name}")
                .lore(customLore)

            // Define actions on clickable player head icon
            lateinit var guiPlayerItem: GuiItem
            guiPlayerItem = GuiItem(playerItem) { guiEvent ->

                // Toggles whitelist state
                if (guiEvent.isLeftClick && canManageWhitelist) {
                    val result = toggleWhitelist.execute(player.uniqueId, warp.id, foundPlayer.uniqueId)
                    result.onSuccess { isWhitelisted ->
                        if (isWhitelisted) {
                            customLore.add(0, "§aWhitelisted")
                        } else {
                            customLore.remove("§aWhitelisted")
                            if (viewMode == 1) {
                                currentPagePane.removeItem(guiPlayerItem)
                            }
                        }
                        playerItem.lore()
                        playerItem.lore(customLore)
                        gui.update()
                    }
                }

                // Opens confirmation menu to ask to revoke access
                else if (guiEvent.isRightClick && getWarpPlayerAccess.execute(warp.id).contains(foundPlayer.uniqueId)) {
                    menuNavigator.openMenu(
                        ConfirmationMenu(menuNavigator, player, "Revoke ${foundPlayer.name}'s access?") {
                            revokeDiscovery.execute(foundPlayer.uniqueId, warp.id)
                            if (viewMode == 0) {
                                currentPagePane.removeItem(guiPlayerItem)
                            }
                        }
                    )
                }
            }

            // Add player menu item
            currentPagePane.addItem(guiPlayerItem)
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