package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.discovery.RevokeDiscovery
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.application.actions.whitelist.ToggleWhitelist
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.createHead
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
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
    private var page = 0
    private var playerNameSearch: String = ""

    enum class DisplayType {
        DISCOVERED,
        WHITELISTED
    }

    override fun open() {
        // Create player access menu
        val gui = ChestGui(6, "Player Access")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = addControlsSection(gui)
        gui.addPane(controlsPane)

        // Switch what players to display depending on view mode, exclude player who owns the warp
        val players = when (viewMode) {
            0 -> getWarpPlayerAccess.execute(warp.id).map { Bukkit.getOfflinePlayer(it) }
            1 -> getPlayerWhitelistForWarp.execute(warp.id).map { Bukkit.getOfflinePlayer(it) }
            2 -> Bukkit.getOnlinePlayers().map { it as OfflinePlayer }
            else -> emptyList()
        }.filter { it.uniqueId != warp.playerId }

        // Filter and display the players
        val playerPane = displayPlayers(players, warp, gui, if (viewMode == 0) DisplayType.DISCOVERED else DisplayType.WHITELISTED)
        gui.addPane(playerPane)

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
        val controlsPane = StaticPane(0, 0, 9, 1)
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
                .name("All Players")
                .lore("Listing all players on the server")
        }
        val guiViewModeItem = GuiItem(viewModeItem) {
            viewMode = (viewMode + 1) % 3 // Cycle through 0, 1, 2
            open()
        }
        controlsPane.addItem(guiViewModeItem, 2, 0)

        // Add search button
        val searchItem = ItemStack(Material.NAME_TAG)
        val guiSearchItem = GuiItem(searchItem) {
            val playerSearchMenu = PlayerSearchMenu(player, menuNavigator)
            menuNavigator.openMenu(playerSearchMenu)
        }
        controlsPane.addItem(guiSearchItem, 3, 0)

        return controlsPane
    }

    private fun displayPlayers(players: List<OfflinePlayer>, warp: Warp,
                               gui: Gui, displayType: DisplayType? = null): StaticPane {
        val playerPane = StaticPane(1, 2, 7, 3)
        var xSlot = 0
        var ySlot = 0

        val whitelisted = getPlayerWhitelistForWarp.execute(warp.id)
        val discovered = getWarpPlayerAccess.execute(warp.id)

        val stockLore = listOf(
            "Left Click to toggle whitelist",
            "Right click to revoke access",
        )

        for (foundPlayer in players) {
            // Modify lore text depending on if the player has discovered this warp or is whitelisted
            val customLore = stockLore.toMutableList()
            if (foundPlayer.uniqueId in discovered) {
                customLore.add(0, "§bDiscovered")
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
                if (guiEvent.isLeftClick) {
                    val result = toggleWhitelist.execute(warp.id, foundPlayer.uniqueId)
                    if (result) {
                        customLore.add(0, "§aWhitelisted")
                    } else {
                        customLore.remove("§aWhitelisted")
                        if (displayType == DisplayType.WHITELISTED) {
                            playerPane.removeItem(guiPlayerItem)
                        }
                    }
                    playerItem.lore()
                    playerItem.lore(customLore)
                    gui.update()
                }

                // Opens confirmation menu to ask to revoke access
                else if (guiEvent.isRightClick) {
                    menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player,
                        "Revoke ${foundPlayer.name}'s access?"
                    ) {
                        revokeDiscovery.execute(foundPlayer.uniqueId, warp.id)
                        if (displayType == DisplayType.DISCOVERED) {
                            playerPane.removeItem(guiPlayerItem)
                        }
                    })
                }
            }

            // Add player menu item
            playerPane.addItem(guiPlayerItem, xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 7) {
                xSlot = 0
                ySlot += 1
            }
        }
        return playerPane
    }
}