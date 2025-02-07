package dev.mizarc.waystonewarps.interaction.menus.use

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.warp.GetPlayerWarpAccess
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.models.toViewModel
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil

class WarpMenu(private val menuNavigator: MenuNavigator): Menu, KoinComponent {
    private val getPlayerWarpAccess: GetPlayerWarpAccess by inject()
    private var page = 1

    override fun open(player: Player) {
        val warps = getPlayerWarpAccess.execute(player.uniqueId)
        val gui = ChestGui(6, "Warps")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back/exit item
        val guiExitItem: GuiItem
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Exit")
        guiExitItem = GuiItem(exitItem) { menuNavigator.goBack(player) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add prev item
        val prevItem = ItemStack(Material.ARROW)
            .name("Prev")
        val guiPrevItem = GuiItem(prevItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPrevItem, 6, 0)

        // Add page item
        val pageItem = ItemStack(Material.PAPER)
            .name("Page $page of ${ceil(warps.count() / 36.0).toInt()}")
        val guiPageItem = GuiItem(pageItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPageItem, 7, 0)

        // Add next item
        val nextItem = ItemStack(Material.ARROW)
            .name("Next")
        val guiNextItem = GuiItem(nextItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiNextItem, 8, 0)

        // Add divider
        val dividerPane = StaticPane(0, 1, 9, 1)
        gui.addPane(dividerPane)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        for (slot in 0..8) {
            val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
            dividerPane.addItem(guiDividerItem, slot, 0)
        }

        // Add list of warps
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (warp in warps) {
            val warpModel = warp.toViewModel()
            val locationText = warpModel.location?.let { location ->
                "${location.blockX}, ${location.blockY}, ${location.blockZ}"
            } ?: run {
                "Location not found"
            }

            val warpItem = ItemStack(warpModel.icon).name(warpModel.name).lore(locationText)
            val guiWarpItem = GuiItem(warpItem) {guiEvent ->
               // teleporter.teleportWarp(player, warp)
                player.closeInventory()
                guiEvent.isCancelled = true
            }
            warpsPane.addItem(guiWarpItem, xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 8) {
                xSlot = 0
                ySlot += 1
            }
        }

        gui.show(player)
    }
}