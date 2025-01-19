package dev.mizarc.waystonewarps.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import dev.mizarc.waystonewarps.Position
import dev.mizarc.waystonewarps.domain.WarpAccessRepository
import dev.mizarc.waystonewarps.domain.waystones.WaystoneRepository
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.block.Block
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

class WarpDestructionListener(val waystoneRepository: WaystoneRepository,
                              val warpAccessRepository: WarpAccessRepository
): Listener {
    @EventHandler
    fun onWarpBreak(event: BlockBreakEvent) {
        if (event.block.type != Material.LODESTONE) return
        val warp = waystoneRepository.getAll().find { it.position == Position(event.block.location) } ?: return

        // Send alert to player until the break count limit is hit
        warp.resetBreakCount()
        if (warp.breakCount > 1) {
            warp.breakCount -= 1
            event.player.sendActionBar(
                Component.text("Break ${warp.breakCount} more times in 10 seconds to destroy this warp")
                    .color(TextColor.color(255, 201, 14)))
            event.isCancelled = true
            return
        }

        // Deletes the warp
        waystoneRepository.remove(warp)
        warpAccessRepository.removeAllAccess(warp)
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        explosionHandler(event.blockList())
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        explosionHandler(event.blockList())
    }

    fun explosionHandler(blocks: MutableList<Block>) {
        for (block in blocks) {
            waystoneRepository.getByPosition(Position(block.location)) ?: continue
            blocks.remove(block)
        }
    }
}