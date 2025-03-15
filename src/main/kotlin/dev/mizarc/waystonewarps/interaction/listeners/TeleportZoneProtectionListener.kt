package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.world.IsPositionInTeleportZone
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TeleportZoneProtectionListener: Listener, KoinComponent {
    private val isPositionInTeleportZone: IsPositionInTeleportZone by inject()

    @EventHandler
    fun blockFallProtection(event: EntityChangeBlockEvent) {
        val fallingBlock = event.entity as? FallingBlock ?: return
        if (event.to == Material.AIR) return

        // Check if in teleport zone
        if (!isPositionInTeleportZone.execute(event.block.location.toPosition3D(), event.block.world.uid)) {
            return
        }

        // Cancel if found to move into teleport zone
        val itemStack = ItemStack(fallingBlock.blockData.material, 1)
        event.isCancelled = true
        event.block.world.dropItemNaturally(fallingBlock.location, itemStack)
    }

    @EventHandler
    fun fluidFlowProtection(event: BlockFromToEvent) {
        // Check if in teleport zone
        if (!isPositionInTeleportZone.execute(event.toBlock.location.toPosition3D(), event.toBlock.world.uid)) {
            return
        }

        event.isCancelled = true
    }
}