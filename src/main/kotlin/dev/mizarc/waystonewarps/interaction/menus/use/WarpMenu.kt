package dev.mizarc.waystonewarps.interaction.menus.use

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import dev.mizarc.waystonewarps.application.actions.discovery.GetFavouritedWarpAccess
import dev.mizarc.waystonewarps.application.actions.teleport.TeleportPlayer
import dev.mizarc.waystonewarps.application.actions.discovery.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.application.actions.management.GetOwnedWarps
import dev.mizarc.waystonewarps.application.actions.management.GetPlayerWarpIcon
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.application.services.WorldGroupService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpAccess
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.admin.WarpGroupManagementMenu
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
import java.util.UUID

class WarpMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider,
    private val groupId: UUID? = null,
    private val groupName: String? = null
): Menu, KoinComponent {
    private val getPlayerWarpAccess: GetPlayerWarpAccess by inject()
    private val teleportPlayer: TeleportPlayer by inject()
    private val getWhitelistedPlayers: GetWhitelistedPlayers by inject()
    private val getFavouritedWarpAccess: GetFavouritedWarpAccess by inject()
    private val getOwnedWarps: GetOwnedWarps by inject()
    private val getPlayerWarpIcon: GetPlayerWarpIcon by inject()
    private val teleportationService: TeleportationService by inject()
    private val configService: ConfigService by inject()
    private val worldGroupService: WorldGroupService? by inject()

    private var viewMode = 0  // 0 = All, Favourites, Owned
    private var page = 1
    private var warpNameSearch: String = ""
    private var usableOnly = true  // if true filters out unusable (other world, locked, etc.)

    override fun open() {
        val title = if (groupName != null) {
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_TITLE_GROUPED, groupName)
        } else {
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_TITLE)
        }
        val gui = ChestGui(6, title)
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
            .let { list -> if (groupId != null) list.filter { it.groupId == groupId } else list }

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
        gui.addPane(Slot.fromXY(1, 2), warpPane)

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
        val outlinePane = OutlinePane(9, 5)
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
        gui.addPane(Slot.fromXY(0, 1), outlinePane)

        // Add controls pane
        val controlsPane = StaticPane(6, 1)
        gui.addPane(Slot.fromXY(0, 0), controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CLOSE_NAME), PrimaryColourPalette.CANCELLED.color!!)
        val guiExitItem = GuiItem(exitItem) { menuNavigator.goBack() }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add groups browse button (slot 1) — or group info label when in group-filtered mode
        if (groupId != null) {
            val groupInfoItem = ItemStack(Material.BOOKSHELF)
                .name(groupName ?: "")
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUPS_BUTTON_LORE))
            controlsPane.addItem(GuiItem(groupInfoItem) { it.isCancelled = true }, 1, 0)
        } else {
            val groupsItem = ItemStack(Material.BOOKSHELF)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUPS_BUTTON_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUPS_BUTTON_LORE))
            controlsPane.addItem(GuiItem(groupsItem) {
                menuNavigator.openMenu(WarpGroupBrowseMenu(player, menuNavigator, localizationProvider))
            }, 1, 0)
        }

        // Add view mode item
        val viewModeItem = when (viewMode) {
            0 -> ItemStack(Material.SUGAR)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_VIEW_MODE_DISCOVERED_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_VIEW_MODE_DISCOVERED_LORE))
            1 -> ItemStack(Material.GLOWSTONE_DUST)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_VIEW_MODE_FAVOURITES_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_VIEW_MODE_FAVOURITES_LORE))
            else -> ItemStack(Material.REDSTONE)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_VIEW_MODE_OWNED_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_VIEW_MODE_OWNED_LORE))
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

        // Add clear/search button depending on state
        if (warpNameSearch.isNotEmpty()) {
            // Add clear search button
            val clearSearchItem = ItemStack(Material.MAGMA_CREAM)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_CLEAR_SEARCH_NAME))
            val guiClearSearchItem = GuiItem(clearSearchItem) {
                warpNameSearch = ""
                page = 1
                open()
            }
            controlsPane.addItem(guiClearSearchItem, 3, 0)
        } else {
            // Add search button
            val searchItem = ItemStack(Material.NAME_TAG)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_SEARCH_NAME))
            val guiSearchItem = GuiItem(searchItem) {
                val warpSearchMenu = WarpSearchMenu(player, menuNavigator, localizationProvider)
                menuNavigator.openMenu(warpSearchMenu)
            }
            controlsPane.addItem(guiSearchItem, 3, 0)
        }

        // Add manage groups button (slot 5) — admin only
        if (player.hasPermission("waystonewarps.admin.manage_groups")) {
            val manageGroupsItem = ItemStack(Material.WRITABLE_BOOK)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_MANAGEMENT_ADMIN_BUTTON_NAME))
                .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_MANAGEMENT_ADMIN_BUTTON_LORE))
            controlsPane.addItem(GuiItem(manageGroupsItem) {
                menuNavigator.openMenu(WarpGroupManagementMenu(player, menuNavigator, localizationProvider))
            }, 5, 0)
        }

        // Add filter item
        val filterItem = when (usableOnly) {
            true -> ItemStack(Material.BUCKET)
                .name(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_FILTER_USABLE_NAME),
                )
                .lore(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_FILTER_USABLE_LORE),
                )

            false -> ItemStack(Material.WATER_BUCKET)
                .name(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_FILTER_UNUSABLE_NAME),
                )
                .lore(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_FILTER_UNUSABLE_LORE),
                )
        }
        val guiFilterItem = GuiItem(filterItem) { guiEvent ->
            // invert usable filter
            usableOnly = !usableOnly
            page = 1
            open()
        }
        controlsPane.addItem(guiFilterItem, 4, 0)

        return controlsPane
    }

    private fun addPaginator(gui: ChestGui, totalPages: Int, page: Int, updateContent: (Int) -> Unit) {
        var currentPage = page // Make currentPage mutable
        val paginatorPane = StaticPane(3, 1)

        fun updatePaginator() {
            paginatorPane.clear()

            // Update page number item
            val pageNumberText = localizationProvider.get(
                player.uniqueId, 
                LocalizationKeys.MENU_COMMON_ITEM_PAGE_NAME,
                currentPage.toString(),
                totalPages.toString()
            )
            val pageNumberItem = ItemStack(Material.PAPER).name(pageNumberText, PrimaryColourPalette.INFO.color!!)
            val guiPageNumberItem = GuiItem(pageNumberItem)
            paginatorPane.addItem(guiPageNumberItem, 1, 0)

            // Update left arrow
            val prevItem: ItemStack
            val guiPrevItem: GuiItem
            if (currentPage <= 1) {
                prevItem = ItemStack(Material.ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME), PrimaryColourPalette.UNAVAILABLE.color!!)
                guiPrevItem = GuiItem(prevItem)
            } else {
                prevItem = ItemStack(Material.SPECTRAL_ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME))
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
                nextItem = ItemStack(Material.ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME),
                        PrimaryColourPalette.UNAVAILABLE.color!!)
                guiNextItem = GuiItem(nextItem)
            } else {
                nextItem = ItemStack(Material.SPECTRAL_ARROW)
                    .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME))
                guiNextItem = GuiItem(nextItem) {
                    currentPage++
                    updateContent(currentPage)
                    updatePaginator()
                    gui.update()
                }
            }
            paginatorPane.addItem(guiNextItem, 2, 0)

        }

        updatePaginator()
        gui.addPane(Slot.fromXY(6, 0), paginatorPane)
    }

    private fun displayWarps(warps: List<Warp>): PaginatedPane {
        val playerPane = PaginatedPane(7, 3)
        var currentPagePane = OutlinePane(7, 3)
        var playerCounter = 0
        val stockLore = listOf(
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_RIGHT_CLICK)
        )

        for (warp in warps) {
            val warpModel = warp.toViewModel()
            val locationText = warpModel.location?.let { location ->
                if (configService.worldNameEnabled()) {
                    "${location.world.name}: ${location.blockX}, ${location.blockY}, ${location.blockZ}"
                } else {
                    "${location.blockX}, ${location.blockY}, ${location.blockZ}"
                }
            } ?: run {
                "Location not found"
            }
            
            val customLore = stockLore.toMutableList()
            if (configService.isTeleportCostEnabled() && configService.getTeleportCostType() == CostType.ITEM) {
                val cost = teleportationService.calculateCost(player.uniqueId, warp)
                if (cost > 0) {
                    customLore.add(0, "§6${localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_COST, cost)}")
                }
            }
            customLore.add(0, "§8$locationText")
            customLore.add(0, "§b${warpModel.player.name}")

            val hasTeleportPermission = player.hasPermission("waystonewarps.teleport")
            val isDifferentWorld = warp.worldId != player.world.uid
            val hasInterworldPermission = !isDifferentWorld || player.hasPermission("waystonewarps.teleport.interworld")
            val hasIntergroupPermission =
                hasInterworldPermission || (
                        player.hasPermission("waystonewarps.teleport.interworldgroup") &&
                        worldGroupService != null &&
                        worldGroupService!!.inSameGroup(warp.worldId, player.world.uid)
                )
            val hasPermission = hasTeleportPermission && (hasInterworldPermission || hasIntergroupPermission)

            // Check if the warp is locked for this player
            val isLocked = warp.accessLevel == WarpAccess.PRIVATE && !getWhitelistedPlayers.execute(warp.id).contains(player.uniqueId) && player.uniqueId != warp.playerId && !player.hasPermission("waystonewarps.bypass.private_access")

            // Skip to next warp if we are filtering
            if (usableOnly) {
                if (!hasPermission || isLocked) {
                    continue
                }
            }

            // Add the locked status if applicable
            if (isLocked) {
                customLore.add(2, "§c${localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_LOCKED)}")
            }

            // Add permission-related lore (only show one message at a time, priority order)
            when {
                !hasTeleportPermission -> {
                    customLore.add(2, "§c${
                        localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_NO_TELEPORT_PERMISSION)
                    }")
                }
                !hasInterworldPermission && !hasIntergroupPermission -> {
                    customLore.add(2, "§c${
                        localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_NO_INTERWORLD_PERMISSION)
                    }")
                }
                isLocked -> {
                    // No additional message needed if locked
                }
                else -> {
                    customLore.add(2, localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_LEFT_CLICK))
                }
            }

            val personalIcon = getPlayerWarpIcon.execute(player.uniqueId, warp.id)
            val warpItem = if (personalIcon != null) {
                ItemStack(Material.valueOf(personalIcon.icon)).applyIconMeta(personalIcon.iconMeta)
            } else {
                ItemStack(warpModel.icon).applyIconMeta(warp.iconMeta)
            }.name(warpModel.name).lore(customLore)

            val guiWarpItem = if (hasPermission && !isLocked) {
                // Player has permission and warp is not locked to them - allow interaction
                GuiItem(warpItem) { guiEvent ->
                    if (guiEvent.isRightClick) {
                        // Right click to open options
                        menuNavigator.openMenu(WarpOptionsMenu(player, menuNavigator, warp, localizationProvider))
                    } else {
                        // Left click to teleport
                        teleportPlayer.execute(
                            player.uniqueId, warp,
                            onPending = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_PENDING, warp.name)
                                    ).color(PrimaryColourPalette.INFO.color)
                                )
                            },
                            onSuccess = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_SUCCESS, warp.name)
                                    ).color(PrimaryColourPalette.SUCCESS.color)
                                )
                            },
                            onFailure = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_FAILED)
                                    ).color(PrimaryColourPalette.FAILED.color)
                                )
                            },
                            onInsufficientFunds = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_INSUFFICIENT_FUNDS)
                                    ).color(PrimaryColourPalette.CANCELLED.color)
                                )
                            },
                            onWorldNotFound = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_WORLD_NOT_FOUND)
                                    ).color(PrimaryColourPalette.FAILED.color)
                                )
                            },
                            onLocked = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_LOCKED)
                                    ).color(PrimaryColourPalette.CANCELLED.color)
                                )
                            },
                            onCanceled = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_CANCELLED)
                                    ).color(PrimaryColourPalette.CANCELLED.color)
                                )
                            },
                            onPermissionDenied = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_NO_PERMISSION)
                                    ).color(PrimaryColourPalette.CANCELLED.color)
                                )
                            },
                            onInterworldPermissionDenied = {
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_NO_INTERWORLD_PERMISSION)
                                    ).color(PrimaryColourPalette.CANCELLED.color)
                                )
                            },
                            onCooldown = { secondsRemaining ->
                                player.sendActionBar(
                                    Component.text(
                                        localizationProvider.get(player.uniqueId, LocalizationKeys.FEEDBACK_TELEPORT_ON_COOLDOWN, secondsRemaining)
                                    ).color(PrimaryColourPalette.CANCELLED.color)
                                )
                            }
                        )
                        player.closeInventory()
                        guiEvent.isCancelled = true
                    }
                }
            } else {
                // Player doesn't have permission or warp is locked - show item but disable interaction
                GuiItem(warpItem) { guiEvent ->
                    // Only allow right-click for options menu if the player has access to the warp
                    if (guiEvent.isRightClick && !(warp.accessLevel == WarpAccess.PRIVATE && !getWhitelistedPlayers.execute(warp.id).contains(player.uniqueId) && player.uniqueId != warp.playerId && !player.hasPermission("waystonewarps.bypass.private_access"))) {
                        menuNavigator.openMenu(WarpOptionsMenu(player, menuNavigator, warp, localizationProvider))
                    } else {
                        // Show appropriate message for left click or no permission
                        if (!hasTeleportPermission) {
                            player.sendActionBar(Component.text(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_NO_TELEPORT_PERMISSION)
                ).color(PrimaryColourPalette.CANCELLED.color))
                        } else if (isDifferentWorld && !hasInterworldPermission) {
                            player.sendActionBar(Component.text(
                                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_NO_INTERWORLD_PERMISSION)
                            ).color(PrimaryColourPalette.CANCELLED.color))
                        } else if (warp.accessLevel == WarpAccess.PRIVATE) {
                            player.sendActionBar(Component.text(
                                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_ITEM_WARP_LORE_LOCKED)
                            ).color(PrimaryColourPalette.CANCELLED.color))
                        }
                    }
                    guiEvent.isCancelled = true
                }
            }

            // Add player menu item
            currentPagePane.addItem(guiWarpItem)
            playerCounter++

            // Check if the current page is full (21 players)
            if (playerCounter >= 21) {
                playerPane.addPage(Slot.fromXY(1, 2), currentPagePane)
                currentPagePane = OutlinePane(7, 3)
                playerCounter = 0
            }
        }

        // Add the last page if it's not empty
        if (playerCounter > 0) {
            playerPane.addPage(Slot.fromXY(0, 0), currentPagePane)
        }

        return playerPane
    }
}
