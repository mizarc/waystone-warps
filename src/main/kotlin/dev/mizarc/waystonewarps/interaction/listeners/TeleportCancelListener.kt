package dev.mizarc.waystonewarps.interaction.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import dev.mizarc.waystonewarps.domain.players.PlayerStateRepository

class TeleportCancelListener(val playerStateRepository: PlayerStateRepository): Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val playerState = playerStateRepository.getByPlayer(event.player) ?: return

        // Do nothing if player is not teleporting
        val teleportTask = playerState.teleportTask ?: return
        if (!playerState.teleportTask!!.isTeleporting()) return


        // Do nothing if player hasn't moved position
        val xPos = event.from.blockX == event.to.blockX
        val yPos = event.from.blockY == event.to.blockY
        val zPos = event.from.blockZ == event.to.blockZ
        if (xPos && yPos && zPos) {
            return
        }

        teleportTask.cancelTask()
        event.player.sendActionBar(
            Component.text("You moved. Teleportation has been cancelled.")
            .color(TextColor.color(255, 85, 85)))
    }
}