package dev.mizarc.waystonewarps.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import dev.mizarc.waystonewarps.Teleporter
import dev.mizarc.waystonewarps.utils.lore
import dev.mizarc.waystonewarps.utils.name
import kotlin.math.ceil

class WarpMenu(private val warpAccessRepository: WarpAccessRepository, private val teleporter: Teleporter,
               private val player: Player) {
    var page = 1

    fun openWarpMenu(backCommand: String? = null) {
        val warps = warpAccessRepository.getByPlayer(player)
        val gui = ChestGui(6, "Warps")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back/exit item
        val guiExitItem: GuiItem
        if (backCommand != null) {
            val exitItem = ItemStack(Material.NETHER_STAR)
                .name("Go Back")
            guiExitItem = GuiItem(exitItem) { player.performCommand(backCommand) }
        }
        else {
            val exitItem = ItemStack(Material.NETHER_STAR)
                .name("Exit")
            guiExitItem = GuiItem(exitItem) { player.closeInventory() }
        }
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
            val warpItem = ItemStack(warp.icon)
                .name(warp.name)
                .lore("${warp.position.x}, ${warp.position.y}, ${warp.position.z}")
            val guiWarpItem = GuiItem(warpItem) {guiEvent ->
                teleporter.teleportWarp(player, warp)
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