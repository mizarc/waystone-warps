package dev.mizarc.waystonewarps.api.events

import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a waystone's properties are updated.
 * @property oldWarp The warp data before the update
 * @property newWarp The warp data after the update
 */
class WarpUpdateEvent(
    val oldWarp: Warp,
    val newWarp: Warp
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
