package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.Scheduler
import dev.mizarc.waystonewarps.application.services.TaskHandle
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

class BukkitSchedulerService(private val plugin: Plugin): Scheduler {
    override fun schedule(delayTicks: Long, task: () -> Unit): TaskHandle {
        val runnable = object : BukkitRunnable() {
            override fun run() {
                task()
            }
        }
        runnable.runTaskLater(plugin, delayTicks)
        return BukkitTaskHandle(runnable)
    }
}

class BukkitTaskHandle(private val runnable: BukkitRunnable) : TaskHandle {
    override fun cancel() {
        runnable.cancel()
    }
}