package dev.mizarc.waystonewarps.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

/**
 * Called when a player discovers a waystone.
 * @property playerId The player who discovered the waystone.
 * @property warpId The waystone that was discovered.
 */
class WarpDiscoverEvent(
    val playerId: UUID,
    val warpId: UUID
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
