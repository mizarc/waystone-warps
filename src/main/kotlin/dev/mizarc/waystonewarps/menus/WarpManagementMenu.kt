package dev.mizarc.waystonewarps.menus

import com.github.stefvanschie.inventoryframework.font.util.Font
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.DispenserGui
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.component.Label
import dev.mizarc.waystonewarps.*
import dev.mizarc.waystonewarps.domain.Warp
import dev.mizarc.waystonewarps.domain.WarpAccessRepository
import dev.mizarc.waystonewarps.domain.waystones.WaystoneRepositorySQLite
import dev.mizarc.waystonewarps.utils.getWarpMoveTool
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.waystonewarps.utils.lore
import dev.mizarc.waystonewarps.utils.name
import org.bukkit.entity.Player
import kotlin.concurrent.thread

                         private val warpAccessRepository: WarpAccessRepository,
                         private val warpBuilder: Warp.Builder) {

    fun openWarpManagementMenu() {
        val existingWarp = waystoneRepositorySQLite.getByPosition(warpBuilder.position)
        if (existingWarp == null) {
            openWarpCreationMenu()
            return
        }

        if (existingWarp.player.uniqueId != warpBuilder.player.uniqueId) return

        openWarpEditMenu(existingWarp)
    }

    fun openWarpCreationMenu() {
        val gui = ChestGui(1, "Warp Creation")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add warp creation icon
        val iconEditorItem = ItemStack(Material.LODESTONE)
            .name("Create Warp")
            .lore("Creating a warp establishes this lodestone as a public teleportation point")
        val guiIconEditorItem = GuiItem(iconEditorItem) { openWarpNamingMenu() }
        pane.addItem(guiIconEditorItem, 4, 0)
        gui.show(Bukkit.getPlayer(warpBuilder.player.uniqueId)!!)
    }

    fun openWarpNamingMenu(existing_name: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui("Naming Warp")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name(warpBuilder.name)
            .lore("${warpBuilder.position.x}, ${warpBuilder.position.y}, ${warpBuilder.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existing_name) {
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
            warpBuilder.name = gui.renameText
            if (waystoneRepositorySQLite.getByName(gui.renameText) != null) {
                openWarpNamingMenu(existing_name = true)
                return@GuiItem
            }
            val warp = warpBuilder.build()
            waystoneRepositorySQLite.add(warp)
            openWarpEditMenu(warp)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(Bukkit.getPlayer(warpBuilder.player.uniqueId)!!)
    }

    fun openWarpEditMenu(warp: Warp) {
        val gui = ChestGui(1, "Warp '${warp.name}'")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add player count icon
        val playerCountItem = ItemStack(Material.PLAYER_HEAD)
            .name("Player Count:")
            .lore("${warpAccessRepository.getByWarp(warp).count()}")
        val guiPlayerCountItem = GuiItem(playerCountItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiPlayerCountItem, 0, 0)

        // Add icon editor button
        val iconEditorItem = ItemStack(warp.icon)
            .name("Edit Warp Icon")
            .lore("Changes the icon that shows up on the warp list")
        val guiIconEditorItem = GuiItem(iconEditorItem) { openWarpIconMenu(warp) }
        pane.addItem(guiIconEditorItem, 2, 0)

        // Add renaming icon
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name("Rename Warp")
            .lore("Renames this warp")
        val guiRenamingItem = GuiItem(renamingItem) { openWarpRenamingMenu(warp) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add direction icon
        val directionItem = ItemStack(Material.COMPASS)
            .name("Change Facing Direction")
            .lore("Alters the direction players face when they get teleported")
        val guiDirectionItem = GuiItem(directionItem) { openWarpDirectionMenu(warp) }
        pane.addItem(guiDirectionItem, 4, 0)

        // Add move icon
        val moveItem = ItemStack(Material.PISTON)
            .name("Move Warp")
            .lore("Place the provided item where you want to move the warp")
        val guiMoveItem = GuiItem(moveItem) { givePlayerMoveTool(warpBuilder.player, warp) }
        pane.addItem(guiMoveItem, 7, 0)

        // Add warp delete icon
        val deleteItem = ItemStack(Material.REDSTONE)
            .name("Delete Warp")
        val guiDeleteItem = GuiItem(deleteItem) { openWarpDeleteMenu(warp) }
        pane.addItem(guiDeleteItem, 8, 0)

        gui.show(warpBuilder.player)
    }

    fun openWarpIconMenu(warp: Warp) {
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
            val temp = guiEvent.cursor
            val cursor = guiEvent.cursor?.type ?: Material.AIR

            if (cursor == Material.AIR) {
                inputPane.removeItem(0, 0)
                gui.update()
                return@setOnClick
            }

            inputPane.addItem(GuiItem(ItemStack(cursor)), 0, 0)
            gui.update()
            thread(start = true) {
                Thread.sleep(1)
                warpBuilder.player.setItemOnCursor(temp)
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
                warp.icon = newIcon.type
                waystoneRepositorySQLite.update(warp)
                openWarpEditMenu(warp)
            }

            // Go back to edit menu if no item in slot
            openWarpEditMenu(warp)
        }
        outputPane.addItem(confirmGuiItem, 0, 0)
        gui.outputComponent.addPane(outputPane)
        gui.show(Bukkit.getPlayer(warpBuilder.player.uniqueId)!!)
    }

    fun openWarpRenamingMenu(warp: Warp, existingName: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui("Renaming Warp")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name(warp.name)
            .lore("${warpBuilder.position.x}, ${warpBuilder.position.y}, ${warpBuilder.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
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
                openWarpEditMenu(warp)
                return@GuiItem
            }

            // Stay on menu if the name is already taken
            if (waystoneRepositorySQLite.getByName(gui.renameText) != null) {
                openWarpRenamingMenu(warp, existingName = true)
                return@GuiItem
            }

            warp.name = gui.renameText
            waystoneRepositorySQLite.update(warp)
            openWarpEditMenu(warp)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(Bukkit.getPlayer(warpBuilder.player.uniqueId)!!)
    }

    fun openWarpDirectionMenu(warp: Warp) {
        val gui = DispenserGui("Select Direction")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add North item
        val northLabel = Label(1, 0, 1, 1, Font.QUARTZ)
        northLabel.setText("N") { _, item ->
            item.name("North")
            GuiItem(item)
        }
        northLabel.setOnClick {
            warp.direction = Direction.NORTH
            waystoneRepositorySQLite.update(warp)
            openWarpEditMenu(warp)
        }
        gui.contentsComponent.addPane(northLabel)

        // Add South item
        val southLabel = Label(1, 2, 1, 1, Font.QUARTZ)
        southLabel.setText("S") { _, item ->
            item.name("South")
            GuiItem(item)
        }
        southLabel.setOnClick {
            warp.direction = Direction.SOUTH
            waystoneRepositorySQLite.update(warp)
            openWarpEditMenu(warp)
        }
        gui.contentsComponent.addPane(southLabel)

        // Add East item
        val eastLabel = Label(2, 1, 1, 1, Font.QUARTZ)
        eastLabel.setText("E") { _, item ->
            item.name("East")
            GuiItem(item)
        }
        eastLabel.setOnClick {
            warp.direction = Direction.EAST
            waystoneRepositorySQLite.update(warp)
            openWarpEditMenu(warp)
        }
        gui.contentsComponent.addPane(eastLabel)

        // Add West item
        val westLabel = Label(0, 1, 1, 1, Font.QUARTZ)
        westLabel.setText("W") { _, item ->
            item.name("West")
            GuiItem(item)
        }
        westLabel.setOnClick {
            warp.direction = Direction.WEST
            waystoneRepositorySQLite.update(warp)
            openWarpEditMenu(warp)
        }
        gui.contentsComponent.addPane(westLabel)

        val infoPane = StaticPane(1, 1, 1, 1)
        val lodestoneItem = ItemStack(Material.NETHER_STAR)
            .name("Current Direction: ")
            .lore(warp.direction.name)
        val guiItem = GuiItem(lodestoneItem) { openWarpEditMenu(warp) }
        infoPane.addItem(guiItem, 0, 0)
        gui.contentsComponent.addPane(infoPane)

        gui.show(Bukkit.getPlayer(warpBuilder.player.uniqueId)!!)
    }

    fun openWarpDeleteMenu(warp: Warp) {
        val gui = HopperGui("Delete Warp?")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(1, 0, 3, 1)
        gui.slotsComponent.addPane(pane)

        // Add no menu item
        val noItem = ItemStack(Material.RED_CONCRETE)
            .name("No")
            .lore("Take me back")
        val guiNoItem = GuiItem(noItem) { guiEvent ->
            guiEvent.isCancelled = true
            openWarpEditMenu(warp)
        }
        pane.addItem(guiNoItem, 0, 0)

        // Add yes menu item
        val yesItem = ItemStack(Material.GREEN_CONCRETE)
            .name("Yes")
            .lore("Warning, this is a permanent action")
        val guiYesItem = GuiItem(yesItem) { guiEvent ->
            guiEvent.isCancelled = true
            warpAccessRepository.removeAllAccess(warp)
            waystoneRepositorySQLite.remove(warp)
            warpBuilder.player.closeInventory()
        }
        pane.addItem(guiYesItem, 2, 0)

        gui.show(warpBuilder.player)
    }

    private fun givePlayerMoveTool(player: Player, warp: Warp) {
        for (item in player.inventory.contents!!) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getWarpMoveTool(warp).itemMeta) {
                return
            }
        }
        player.inventory.addItem(getWarpMoveTool(warp))
    }
}