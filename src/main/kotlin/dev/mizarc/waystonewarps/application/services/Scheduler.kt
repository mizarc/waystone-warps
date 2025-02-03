package dev.mizarc.waystonewarps.application.services

/**
 * Schedules an event to run after an X amount of time
 */
interface Scheduler {
    fun schedule(delayTicks: Long, task: () -> Unit): TaskHandle
}

/**
 * Represents a task that is to be performed
 */
interface TaskHandle {
    fun cancel()
}