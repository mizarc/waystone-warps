package dev.mizarc.waystonewarps.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.application.actions.warp.CreateWarp
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class WarpNamingMenu(private val menuNavigator: MenuNavigator, private val location: Location): Menu, KoinComponent {
    private val createWarp: CreateWarp by inject()

    private var nameAttempt = ""

    override fun open(player: Player) {
        // Create homes menu
        val gui = AnvilGui("Naming Warp")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name(nameAttempt)
            .lore("${location.blockX}, ${location.blockY}, ${location.blockZ}")
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
            val result = createWarp.execute(player.uniqueId, gui.renameText,
                location.toPosition3D(), location.world.uid)
            when (result) {
                is CreateWarpResult.Success -> {
                    replaceBottomBlockWithBarrier(result.warp)
                    generateCustomModel(result.warp)
                    menuNavigator.openMenu(player, WarpManagementMenu(menuNavigator, result.warp))
                }
                else -> {
                    nameAttempt = gui.renameText
                    open(player)
                }
            }
        }

        // GUI display
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }

    private fun replaceBottomBlockWithBarrier(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)

        val bottomBlock = world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ)
        bottomBlock.type = Material.BARRIER
    }

    private fun generateCustomModel(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE_SLAB,
            0.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE,
            0.075f, 0.8f, 0.075f,
            0.85f, 0.85f, 0.85f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE,
            0.2f, 0.4f, 0.2f,
            0.6f, 0.6f, 0.6f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE,
            0.075f, 1.3f, 0.075f,
            0.85f, 0.85f, 0.85f)
    }

    private fun createBlockDisplay(warpId: UUID, baseLocation: Location, material: Material,
                                   offsetX: Float, offsetY: Float, offsetZ: Float,
                                   scaleX: Float, scaleY: Float, scaleZ: Float) {
        // Create BlockData
        val blockData = material.createBlockData()
        baseLocation.y -= 1
        val blockDisplay = baseLocation.world.spawnEntity(baseLocation, EntityType.BLOCK_DISPLAY) as BlockDisplay
        blockDisplay.block = blockData

        // Transform display
        val transformation = Transformation(
            Vector3f(offsetX, offsetY, offsetZ), AxisAngle4f(),
            Vector3f(scaleX, scaleY, scaleZ), AxisAngle4f())
        blockDisplay.transformation = transformation
        blockDisplay.customName(Component.text((warpId.toString())))
    }
}