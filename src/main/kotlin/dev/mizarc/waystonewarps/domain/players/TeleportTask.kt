package dev.mizarc.waystonewarps.domain.players

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TeleportTask(val player: Player, val location: Location): BukkitRunnable() {

    override fun run() {
        player.teleport(location)
    }
}