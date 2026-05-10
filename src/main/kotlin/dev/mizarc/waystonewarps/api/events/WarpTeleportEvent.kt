package dev.mizarc.waystonewarps.api.events

import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

/**
 * Called when a player successfully teleports to a waystone.
 * @property playerId The player who teleported.
 * @property warp The destination waystone.
 */
class WarpTeleportEvent(
    val playerId: UUID,
    val warp: Warp
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
