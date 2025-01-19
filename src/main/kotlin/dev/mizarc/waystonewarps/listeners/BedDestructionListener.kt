package dev.mizarc.waystonewarps.listeners

import org.bukkit.block.data.type.Bed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import dev.mizarc.waystonewarps.domain.HomeRepository
import dev.mizarc.waystonewarps.Position
import org.bukkit.block.Block
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

class BedDestructionListener(val homes: HomeRepository): Listener {

    @EventHandler
    fun onBedDestroy(event: BlockBreakEvent) {
        if (event.block.blockData !is Bed) {
            return
        }

        val bed = event.block.blockData as Bed
        val otherHalf = event.block.getRelative(bed.facing)

        var home = homes.getByPosition(Position(event.block.location))
        if (home != null) {
            homes.remove(home)
        }

        home = homes.getByPosition(Position(otherHalf.location))
        if (home != null) {
            homes.remove(home)
        }
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
            val home = homes.getByPosition(Position(block.location)) ?: continue
            homes.remove(home)
        }
    }
}