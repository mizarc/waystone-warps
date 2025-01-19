package dev.mizarc.waystonewarps.listeners

import dev.mizarc.waystonewarps.Position
import dev.mizarc.waystonewarps.infrastructure.services.playerlimit.persistence.waystones.WaystoneRepositorySQLite
import dev.mizarc.waystonewarps.utils.getStringMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class WarpMoveToolListener(private val warpRepo: WaystoneRepositorySQLite): Listener {

    @EventHandler
    fun onWarpMoveBlockPlace(event: BlockPlaceEvent) {
        val warpId = event.itemInHand.getStringMeta("warp") ?: return
        val warp = warpRepo.getById(UUID.fromString(warpId)) ?: return
        val world = warp.getWorld() ?: return

        val existingLocation = Location(world,
            warp.position.x.toDouble(), warp.position.y.toDouble(), warp.position.z.toDouble())
        val existingBlock = existingLocation.block
        existingBlock.breakNaturally(ItemStack(Material.WOODEN_HOE))
        warp.position = Position(event.blockPlaced.location)
        warpRepo.update(warp)
        event.isCancelled = false
        event.player.sendActionBar(
            Component.text("Warp position has been moved")
                .color(TextColor.color(85, 255, 85)))
    }
}