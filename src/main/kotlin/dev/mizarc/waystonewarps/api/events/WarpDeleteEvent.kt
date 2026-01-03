package dev.mizarc.waystonewarps.api.events

import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a waystone is deleted.
 * @property warp The warp that was deleted
 */
class WarpDeleteEvent(
    val warp: Warp
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
