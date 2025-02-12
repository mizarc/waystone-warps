package dev.mizarc.waystonewarps.infrastructure.services.scheduling

import dev.mizarc.waystonewarps.application.services.scheduling.Task
import org.bukkit.scheduler.BukkitRunnable

class TaskBukkit(private val runnable: BukkitRunnable) : Task {
    override fun cancel() {
        runnable.cancel()
    }
}