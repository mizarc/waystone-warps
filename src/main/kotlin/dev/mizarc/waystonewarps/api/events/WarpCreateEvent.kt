package dev.mizarc.waystonewarps.api.events

import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a new waystone is created.
 * @property warp The warp that was created
 */
class WarpCreateEvent(
    val warp: Warp
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
