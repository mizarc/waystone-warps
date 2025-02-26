package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpNamingMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                     private val location: Location): Menu, KoinComponent {
    private val createWarp: CreateWarp by inject()
    private var name = ""
    private var isConfirming = false

    override fun open() {
        // Create homes menu
        val gui = AnvilGui("Naming Warp")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnNameInputChanged { newName ->
            if (!isConfirming) {
                name = newName
            } else {
                isConfirming = false
            }
        }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name("")
            .lore("${location.blockX}, ${location.blockY}, ${location.blockZ}")
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
            val result = createWarp.execute(player.uniqueId, name,
                location.toPosition3D(), location.world.uid)
            when (result) {
                is CreateWarpResult.Success -> {
                    menuNavigator.openMenu(WarpManagementMenu(player, menuNavigator, result.warp))
                }
                is CreateWarpResult.LimitExceeded -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("You've already hit your maximum warp limit")
                    val guiPaperItem = GuiItem(paperItem) {guiEvent ->
                        secondPane.removeItem(0, 0)
                        lodestoneItem.name(name)
                        isConfirming = true
                        gui.update()
                    }
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(name)
                    isConfirming = true
                    gui.update()
                }
                is CreateWarpResult.NameAlreadyExists -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("That name has already been taken")
                    val guiPaperItem = GuiItem(paperItem) {guiEvent ->
                        secondPane.removeItem(0, 0)
                        lodestoneItem.name(name)
                        isConfirming = true
                        gui.update()
                    }
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(name)
                    isConfirming = true
                    gui.update()
                }
                is CreateWarpResult.NameCannotBeBlank -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("Name cannot be blank")
                    val guiPaperItem = GuiItem(paperItem) {guiEvent ->
                        secondPane.removeItem(0, 0)
                        lodestoneItem.name(name)
                        isConfirming = true
                        gui.update()
                    }
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name("")
                    gui.update()
                }
            }
        }

        // GUI display
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}