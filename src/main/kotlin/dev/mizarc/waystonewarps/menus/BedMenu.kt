package dev.mizarc.waystonewarps.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.geco.gsit.api.GSitAPI
import dev.geco.gsit.objects.GetUpReason
import org.apache.commons.lang.WordUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.Bed
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.ItemStack
import dev.mizarc.waystonewarps.*
import dev.mizarc.waystonewarps.infrastructure.services.playerlimit.VaultPlayerLimitServiceImpl
import dev.mizarc.waystonewarps.utils.lore
import dev.mizarc.waystonewarps.utils.name
import dev.mizarc.waystonewarps.utils.toBed

class BedMenu(private val homes: HomeRepository, private val vaultPlayerLimitServiceImpl: VaultPlayerLimitServiceImpl,
              private val homeBuilder: Home.Builder) {
    fun openHomeSelectionMenu() {
        // Create homes menu
        val gui = ChestGui(1, "Homes")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)
        var lastPaneEntry = 1

        // Add bed player is currently sleeping in
        val existingHome = homes.getAll().find { it.position == homeBuilder.position }
        val guiCurrentBedItem: GuiItem
        val currentBedItem = ItemStack(homeBuilder.bed.material)
            .name("Current Home")
            .lore("You will respawn at this bed " +
                    "(${homeBuilder.position.x} / ${homeBuilder.position.y} / ${homeBuilder.position.z})")

        // Change bed info depending on who owns the bed, or if bed is unowned
        if (existingHome != null) {
            if (existingHome.player.uniqueId == vaultPlayerLimitServiceImpl.player.uniqueId) {
                currentBedItem.lore("This home belongs to you. Click to edit.")
                guiCurrentBedItem = GuiItem(currentBedItem) { _ -> openHomeEditMenu(homeBuilder, existingHome) }
            }
            else {
                currentBedItem.lore("This home belongs to ${existingHome.player.name}.")
                guiCurrentBedItem = GuiItem(currentBedItem) { guiEvent -> guiEvent.isCancelled = true }
            }
        }
        else {
            guiCurrentBedItem = GuiItem(currentBedItem) { guiEvent -> guiEvent.isCancelled = true }
        }
        pane.addItem(guiCurrentBedItem, 0, 0)

        // Add separator
        val separator = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guisSeparator = GuiItem(separator) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guisSeparator, 1, 0)

        // Add existing homes to menu
        val playerHomes = homes.getByPlayer(vaultPlayerLimitServiceImpl.player)
        if (playerHomes.isNotEmpty()) {
            var lastIndex = 0
            for (home in playerHomes) {
                if (home.position == homeBuilder.position) {
                    continue
                }

                val bedItem = ItemStack(home.colour.toBed())
                    .name(home.name)
                    .lore("Teleports to bed at ${home.position.x}, ${home.position.y}, ${home.position.z}.")
                val guiBedItem = GuiItem(bedItem) { _ ->
                    vaultPlayerLimitServiceImpl.inBedMenu = false
                    GSitAPI.removePose(homeBuilder.player, GetUpReason.GET_UP)
                    teleportToBed(homeBuilder.player, home)
                }
                pane.addItem(guiBedItem, lastIndex + 2, 0)
                lastPaneEntry = lastIndex + 2
                lastIndex += 1
            }
        }

        // Sets new home item based on home state
        if (playerHomes.count() < vaultPlayerLimitServiceImpl.getHomeLimit())
        {
            val guiItem = if (isHomeAlreadySet(homeBuilder.position)) {
                val newBedItem = ItemStack(Material.MAGMA_CREAM)
                    .name("Home already set")
                    .lore("You cannot set an already saved home.")
                GuiItem(newBedItem) { guiEvent -> guiEvent.isCancelled = true }
            }
            else if (isHomeAlreadyOwned(homeBuilder.position)) {
                val newBedItem = ItemStack(Material.MAGMA_CREAM)
                    .name("Home already owned")
                    .lore("You cannot set home owned by someone else.")
                GuiItem(newBedItem) { guiEvent -> guiEvent.isCancelled = true }
            }
            else {
                val newBedItem = ItemStack(Material.NETHER_STAR)
                    .name("Add new home")
                    .lore("Sets your current bed as a saved home.")
                GuiItem(newBedItem) { openHomeCreationMenu(homeBuilder) }
            }
            pane.addItem(guiItem, lastPaneEntry + 1, 0)
        }

        gui.show(homeBuilder.player)
        vaultPlayerLimitServiceImpl.inBedMenu = true
    }

    fun openHomeCreationMenu(homeBuilder: Home.Builder) {
        // Create homes menu
        val gui = AnvilGui("Name your home")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add bed menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val bedItem = ItemStack(homeBuilder.bed.material)
            .lore("${homeBuilder.position.x}, ${homeBuilder.position.y}, ${homeBuilder.position.z}")
        val guiItem = GuiItem(bedItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add confirm menu item.
        val secondPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            homes.add(homeBuilder.name(gui.renameText).build())
            openHomeSelectionMenu()
        }
        secondPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(secondPane)
        gui.show(homeBuilder.player)
    }

    fun openHomeEditMenu(homeBuilder: Home.Builder, editingHome: Home) {
        // Create edit menu
        val name = editingHome.name.ifEmpty {
            WordUtils.capitalizeFully(editingHome.colour.toBed().name.replace("_", " "))
        }
        val gui = ChestGui(1, "Editing $name")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add Edit button
        val renameItem = ItemStack(Material.NAME_TAG)
            .name("Rename Home")
        val guiRenameItem = GuiItem(renameItem) {
            openHomeRenameMenu(homeBuilder, editingHome) }
        pane.addItem(guiRenameItem, 2, 0)

        // Add Remove button
        val removeItem = ItemStack(Material.REDSTONE)
            .name("Delete Home")
        val guiRemoveItem = GuiItem(removeItem) {
            homes.remove(editingHome)
            openHomeSelectionMenu()
        }
        pane.addItem(guiRemoveItem, 4, 0)

        // Add Go Back button
        val goBackItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiGoBackItem = GuiItem(goBackItem) {
            openHomeSelectionMenu() }
        pane.addItem(guiGoBackItem, 6, 0)
        gui.show(homeBuilder.player)
    }

    fun openHomeRenameMenu(homeBuilder: Home.Builder, editingHome: Home) {
        // Create homes menu
        val gui = AnvilGui("Renaming ${editingHome.name}")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add bed menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val bedItem = ItemStack(
            editingHome.colour.toBed().createBlockData().material)
            .lore("${editingHome.position.x}, ${editingHome.position.y}, ${editingHome.position.z}")
        val guiItem = GuiItem(bedItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add confirm menu item.
        val secondPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            val newHome = Home(editingHome.id, editingHome.player, editingHome.creationTime,
                gui.renameText, editingHome.colour, editingHome.worldId, editingHome.position, editingHome.direction)
            homes.update(newHome)
            openHomeEditMenu(homeBuilder, editingHome)
            guiEvent.isCancelled = true
        }
        secondPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(secondPane)
        gui.show(Bukkit.getPlayer(editingHome.player.uniqueId)!!)
    }

    private fun isHomeAlreadySet(position: Position): Boolean {
        val playerHomes = homes.getByPlayer(vaultPlayerLimitServiceImpl.player)
        for (home in playerHomes) {
            if (position == home.position) {
                return true
            }
        }
        return false
    }

    private fun isHomeAlreadyOwned(position: Position): Boolean {
        val allHomes = homes.getAll()
        for (home in allHomes) {
            if (position == home.position) {
                return true
            }
        }
        return false
    }

    private fun teleportToBed(player: Player, home: Home) {
        // Set player's view to align with the bed
        val sleepingLocation = Location(home.getWorld(), home.position.x.toDouble(), home.position.y.toDouble(), home.position.z.toDouble())
        val bed = sleepingLocation.block.blockData as Bed
        val direction = Direction.fromVector(bed.facing.oppositeFace.direction)
        val world = home.getWorld() ?: return
        player.teleport(Location(home.getWorld(), home.position.x.toDouble(), home.position.y.toDouble() + 1, home.position.z.toDouble(), Direction.toYaw(direction), 0.0f))
        GSitAPI.createPose(sleepingLocation.block, player, Pose.SLEEPING,
            0.0, 0.0, 0.0, Direction.toYaw(home.direction), true)
        player.bedSpawnLocation = home.position.toLocation(world)
    }
}