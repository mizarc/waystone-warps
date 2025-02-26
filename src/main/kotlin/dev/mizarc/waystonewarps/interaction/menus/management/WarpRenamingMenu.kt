package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpRenamingMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                       private val warp: Warp): Menu, KoinComponent {
    private val updateWarpName: UpdateWarpName by inject()

    override fun open() {
        // Create homes menu
        val gui = AnvilGui("Renaming Warp")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

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
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (gui.renameText == warp.name) {
                menuNavigator.goBack()
                return@GuiItem
            }

            // Attempt renaming
            val result = updateWarpName.execute(warp.id, player.uniqueId, gui.renameText)
            when (result) {
                UpdateWarpNameResult.SUCCESS -> menuNavigator.goBack()
                UpdateWarpNameResult.WARP_NOT_FOUND -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("The warp you are trying to rename does not exist anymore")
                    val guiPaperItem = GuiItem(paperItem)
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(gui.renameText)
                    gui.update()
                }
                UpdateWarpNameResult.NAME_ALREADY_TAKEN -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("That name has already been taken")
                    val guiPaperItem = GuiItem(paperItem)
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(gui.renameText)
                    gui.update()
                }
                UpdateWarpNameResult.NAME_BLANK -> menuNavigator.goBack()
            }
        }

        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}