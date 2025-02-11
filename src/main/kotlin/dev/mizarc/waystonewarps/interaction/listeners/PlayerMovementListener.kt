package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.teleport.LogPlayerMovement
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlayerMovementListener: Listener, KoinComponent {
    private val logPlayerMovement: LogPlayerMovement by inject()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to

        // Check if the player has moved from one block to another
        if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
            logPlayerMovement.execute(event.player.uniqueId)
        }
    }
}