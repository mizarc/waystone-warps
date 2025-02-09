package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.warp.UpdateWarpName
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

class WarpRenamingMenu(private val menuNavigator: MenuNavigator, private val warp: Warp): Menu, KoinComponent {
    private val updateWarpName: UpdateWarpName by inject()
    private var nameAttempt = ""

    override fun open(player: Player) {
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
        if (nameAttempt != "") {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name("That name has already been taken")
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (gui.renameText == warp.name) {
                menuNavigator.goBack(player)
                return@GuiItem
            }

            // Stay on menu if the name is already taken
            val result = updateWarpName.execute(warp.id, gui.renameText)
            if (result.isFailure) {
                nameAttempt = gui.renameText
                open(player)
                return@GuiItem
            }

           menuNavigator.goBack(player)
        }

        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}