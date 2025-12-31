package dev.mizarc.waystonewarps.interaction.listeners

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import dev.mizarc.waystonewarps.application.actions.world.BreakWarpBlock
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.results.BreakWarpResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class WaystoneDestructionListener: Listener, KoinComponent {
    private val getWarpAtPosition: GetWarpAtPosition by inject()
    private val breakWarpBlock: BreakWarpBlock by inject()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onClaimHubDestroy(event: BlockBreakEvent) {
        val bottomBlockPosition = event.block.location.toPosition3D()
        val topBlockPosition = event.block.location.clone().apply { y += 1 }.toPosition3D()

        // Break and perform action based on result
        val positions = listOf(topBlockPosition, bottomBlockPosition)
        for (position in positions) {
            val result = breakWarpBlock.execute(position, event.block.world.uid)
            when (result) {
                is BreakWarpResult.Success -> triggerSuccess(event.player, result.warp)
                is BreakWarpResult.Breaking -> {
                    event.player.sendActionBar(
                        Component.text("Break ${result.breaksRemaining} more times in 10 seconds " +
                                "to destroy this waystone").color(TextColor.color(255, 201, 14)))
                    event.isCancelled = true
                }
                else -> continue
            }
        }
    }


    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        val blocks = explosionHandler(event.blockList())
        event.blockList().removeAll(blocks)
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val blocks = explosionHandler(event.blockList())
        event.blockList().removeAll(blocks)
    }

    @EventHandler
    fun onPistolPush(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            val isCurrentBlock = getWarpAtPosition.execute(block.location.toPosition3D(), event.block.world.uid) != null
            val blockAbove = event.block.world.getBlockAt(block.location.add(0.0, 1.0, 0.0)).location
            val isBlockAbove = getWarpAtPosition.execute(blockAbove.toPosition3D(), blockAbove.world.uid) != null

            if (isCurrentBlock || isBlockAbove) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPistolPull(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            val isCurrentBlock = getWarpAtPosition.execute(block.location.toPosition3D(), block.world.uid) != null
            val blockAbove = event.block.world.getBlockAt(block.location.add(0.0, 1.0, 0.0)).location
            val isBlockAbove = getWarpAtPosition.execute(blockAbove.toPosition3D(), blockAbove.world.uid) != null

            if (isCurrentBlock || isBlockAbove) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockDestroy(event: BlockDestroyEvent) {
        val block = event.block
        val isCurrentBlock = getWarpAtPosition.execute(block.location.toPosition3D(), event.block.world.uid) != null
        val blockAbove = event.block.world.getBlockAt(block.location.add(0.0, 1.0, 0.0)).location
        val isBlockAbove = getWarpAtPosition.execute(blockAbove.toPosition3D(), blockAbove.world.uid) != null

        if (isCurrentBlock || isBlockAbove) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onTreeGrowth(event: StructureGrowEvent) {
        for (block in event.blocks) {
            val isCurrentBlock = getWarpAtPosition.execute(block.location.toPosition3D(), block.world.uid) != null
            val blockAbove = block.world.getBlockAt(block.location.add(0.0, 1.0, 0.0)).location
            val isBlockAbove = getWarpAtPosition.execute(blockAbove.toPosition3D(), blockAbove.world.uid) != null

            if (isCurrentBlock || isBlockAbove) {
                event.isCancelled = true
            }
        }
    }

    fun explosionHandler(blocks: MutableList<Block>): List<Block> {
        val cancelledBlocks = mutableListOf<Block>()
        for (block in blocks) {
            if (getWarpAtPosition.execute(block.location.toPosition3D(), block.world.uid) != null) {
                cancelledBlocks.add(block)
            }

            val blockAbove = block.world.getBlockAt(block.location.add(0.0, 1.0, 0.0)).location
            val isBlockAbove = getWarpAtPosition.execute(blockAbove.toPosition3D(), blockAbove.world.uid)
            if (isBlockAbove != null) {
                cancelledBlocks.add(block)
            }
        }
        return cancelledBlocks
    }

    private fun triggerSuccess(player: Player, warp: Warp) {
        // Remove any move objects in player inventory
        for ((index, item) in player.inventory.withIndex()) {
            if (item == null) continue
            val itemMeta = item.itemMeta ?: continue
            val warpText = itemMeta.persistentDataContainer.get(
                NamespacedKey("waystonewarps","warp"), PersistentDataType.STRING) ?: continue
            val warpId = UUID.fromString(warpText) ?: continue
            if (warpId == warp.id) {
                if (index == 40) {
                   player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                }
                else {
                    player.inventory.remove(item)
                }
            }
        }

        // Send message when the warp is broken
        player.sendActionBar(
            Component.text("Waystone '${warp.name}' has been destroyed")
                .color(TextColor.color(85, 255, 85)))
    }
}