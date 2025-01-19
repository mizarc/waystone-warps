package dev.mizarc.waystonewarps

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

class TeleportTask(var task: BukkitTask, var player: Player, var location: Location) {

    fun isTeleporting(): Boolean {
        return !task.isCancelled
    }

    fun cancelTask() {
        task.cancel()
    }
}
