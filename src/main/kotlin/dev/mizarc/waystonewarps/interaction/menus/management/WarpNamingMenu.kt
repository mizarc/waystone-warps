package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpNamingMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val location: Location
) : Menu, KoinComponent {
    private val createWarp: CreateWarp by inject()
    private val localizationProvider: LocalizationProvider by inject()
    private var name = ""
    private var isConfirming = false

    override fun open() {
        val title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_NAMING_TITLE)
        val gui = AnvilGui(title)
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
            .lore(localizationProvider.get(
                player.uniqueId,
                LocalizationKeys.MENU_WARP_NAMING_ITEM_WARP_LORE,
                location.blockX.toString(),
                location.blockY.toString(),
                location.blockZ.toString()
            ))
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        val secondPane = StaticPane(0, 0, 1, 1)
        gui.secondItemComponent.addPane(secondPane)

        // Add confirm menu item
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME))

        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            val belowLocation = location.clone().subtract(0.0, 1.0, 0.0)
            val result = createWarp.execute(
                player.uniqueId,
                name,
                location.toPosition3D(),
                location.world.uid,
                location.world.getBlockAt(belowLocation).type.name
            )

            when (result) {
                is CreateWarpResult.Success -> {
                    location.world.playSound(
                        player.location,
                        Sound.BLOCK_VAULT_OPEN_SHUTTER,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f
                    )
                    menuNavigator.openMenu(WarpManagementMenu(player, menuNavigator, result.warp))
                }
                is CreateWarpResult.LimitExceeded -> {
                    showErrorMessage(
                        gui,
                        secondPane,
                        localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_LIMIT)
                    )
                }
                is CreateWarpResult.NameAlreadyExists -> {
                    showErrorMessage(
                        gui,
                        secondPane,
                        localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_EXISTING)
                    )
                }
                is CreateWarpResult.NameCannotBeBlank -> {
                    showErrorMessage(
                        gui,
                        secondPane,
                        localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_BLANK)
                    )
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

    private fun showErrorMessage(gui: AnvilGui, pane: StaticPane, message: String) {
        val paperItem = ItemStack(Material.PAPER).name(message)
        val guiPaperItem = GuiItem(paperItem) {
            pane.removeItem(0, 0)
            isConfirming = true
            gui.update()
        }
        pane.addItem(guiPaperItem, 0, 0)
        isConfirming = true
        gui.update()
    }
}