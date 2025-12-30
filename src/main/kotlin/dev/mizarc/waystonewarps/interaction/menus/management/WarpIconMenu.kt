package dev.mizarc.waystonewarps.interaction.menus.management

import IconMeta
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpIcon
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.thread

class WarpIconMenu(private val player: Player,
                   private val menuNavigator: MenuNavigator, private val warp: Warp): Menu, KoinComponent {
    private val updateWarpIcon: UpdateWarpIcon by inject()

    override fun open() {
        val gui = FurnaceGui("Set Warp Icon")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val fuelPane = StaticPane(0, 0, 1, 1)

        // Add info paper menu item
        val paperItem = ItemStack(Material.PAPER)
            .name("Place an item in the top slot to set it as the icon")
            .lore("Don't worry, you'll get the item back")
        val guiIconEditorItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
        fuelPane.addItem(guiIconEditorItem, 0, 0)
        gui.fuelComponent.addPane(fuelPane)

        // Allow item to be placed in slot
        val inputPane = StaticPane(0, 0, 1, 1)
        inputPane.setOnClick {guiEvent ->
            guiEvent.isCancelled = true
            val itemOnCursor = guiEvent.cursor

            if (itemOnCursor.type == Material.AIR) {
                inputPane.removeItem(0, 0)
                gui.update()
                return@setOnClick
            }

            inputPane.addItem(GuiItem(ItemStack(itemOnCursor.clone())), 0, 0)
            gui.update()
            thread(start = true) {
                Thread.sleep(1)
                player.setItemOnCursor(itemOnCursor)
            }
        }
        gui.ingredientComponent.addPane(inputPane)

        // Add confirm menu item
        val outputPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            val newIcon = gui.ingredientComponent.getItem(0, 0)

            // Set icon if item in slot
            if (newIcon != null) {
                val paperCmd = newIcon.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
                val potionTypeKey = (newIcon.itemMeta as? PotionMeta)
                    ?.basePotionType
                    ?.key
                    ?.toString()
                println("potionTypeKey:")
                println(potionTypeKey)

                val leatherColorRgb = (newIcon.itemMeta as? LeatherArmorMeta)
                    ?.color
                    ?.asRGB()
                println("leatherColorRgb:")
                println(leatherColorRgb)
                val iconMeta = if (paperCmd != null) {
                    IconMeta(
                        strings = paperCmd.strings(),
                        floats = paperCmd.floats(),
                        flags = paperCmd.flags(),
                        colorsArgb = paperCmd.colors().map { it.asARGB() },
                        potionTypeKey = potionTypeKey,
                        leatherColorRgb = leatherColorRgb
                    )
                } else {
                    IconMeta(
                        potionTypeKey = potionTypeKey,
                        leatherColorRgb = leatherColorRgb
                    )
                }
                println(iconMeta)
                updateWarpIcon.execute(warp.id, newIcon.type.name, iconMeta)
            }

            // Go back to edit menu
            menuNavigator.goBack()
        }
        outputPane.addItem(confirmGuiItem, 0, 0)
        gui.outputComponent.addPane(outputPane)
        gui.show(player)
    }
}