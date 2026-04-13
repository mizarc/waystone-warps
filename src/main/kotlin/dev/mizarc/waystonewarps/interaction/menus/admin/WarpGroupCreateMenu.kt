package dev.mizarc.waystonewarps.interaction.menus.admin

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroupResult
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpGroupCreateMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val createWarpGroup: CreateWarpGroup by inject()

    private var groupName = ""
    private var isConfirming = false

    override fun open() {
        val gui = AnvilGui(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_CREATE_TITLE))
        gui.setOnTopClick { it.isCancelled = true }
        gui.setOnNameInputChanged { newName ->
            if (!isConfirming) groupName = newName else isConfirming = false
        }

        val firstPane = StaticPane(0, 0, 1, 1)
        val bookItem = ItemStack(Material.BOOKSHELF)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_MANAGEMENT_CREATE_NAME))
        firstPane.addItem(GuiItem(bookItem) { it.isCancelled = true }, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        val secondPane = StaticPane(0, 0, 1, 1)
        gui.secondItemComponent.addPane(secondPane)

        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME), PrimaryColourPalette.SUCCESS.color!!)
        val confirmGuiItem = GuiItem(confirmItem) {
            when (createWarpGroup.execute(player.uniqueId, groupName)) {
                CreateWarpGroupResult.SUCCESS -> menuNavigator.goBack()
                CreateWarpGroupResult.NAME_BLANK -> menuNavigator.goBack()
                CreateWarpGroupResult.NAME_TAKEN -> {
                    val errorItem = ItemStack(Material.PAPER)
                        .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_NAME_TAKEN), PrimaryColourPalette.FAILED.color!!)
                    secondPane.addItem(GuiItem(errorItem) {
                        secondPane.removeItem(0, 0)
                        isConfirming = true
                        gui.update()
                    }, 0, 0)
                    isConfirming = true
                    gui.update()
                }
            }
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}
