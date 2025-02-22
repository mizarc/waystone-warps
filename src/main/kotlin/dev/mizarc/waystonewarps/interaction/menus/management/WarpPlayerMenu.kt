package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import dev.mizarc.waystonewarps.application.actions.discovery.GetWarpPlayerAccess
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
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

    private var viewMode = 0  // 0 = Discovered, 1 = Whitelisted, 2 = All
    private var page = 0
    private var playerNameSearch: String = ""

    override fun open() {
        // Create player access menu
        val gui = ChestGui(6, "Player Access")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = addControlsSection(gui)
        gui.addPane(controlsPane)

        // Switch what players to display depending on view mode
        val playerPane = when (viewMode) {
            0 -> displayPlayers(getWarpPlayerAccess.execute(warp.id).mapNotNull { Bukkit.getPlayer(it) })
            1 -> displayPlayers(getPlayerWhitelistForWarp.execute(warp.id).mapNotNull { Bukkit.getPlayer(it) })
            2 -> displayPlayers(Bukkit.getOnlinePlayers().toList())
            else -> StaticPane(1, 2, 7, 3)
        }
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

    private fun displayPlayers(players: List<OfflinePlayer>): StaticPane {
        val playerPane = StaticPane(1, 2, 7, 3)
        var xSlot = 0
        var ySlot = 0

        for (foundPlayer in players) {
            // Create player menu item
            val playerItem = createHead(foundPlayer)
                .name("${foundPlayer.name}")
            val guiPlayerItem = GuiItem(playerItem) {
                // Pass
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