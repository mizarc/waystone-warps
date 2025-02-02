package dev.mizarc.waystonewarps.application.services

interface Scheduler {
    fun schedule(delayTicks: Long, task: () -> Unit): TaskHandle
}

interface TaskHandle {
    fun cancel()
}