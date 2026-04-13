package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.api.events.WarpCreateEvent
import dev.mizarc.waystonewarps.api.events.WarpDeleteEvent
import dev.mizarc.waystonewarps.api.events.WarpDiscoverEvent
import dev.mizarc.waystonewarps.api.events.WarpTeleportEvent
import dev.mizarc.waystonewarps.api.events.WarpUpdateEvent
import dev.mizarc.waystonewarps.api.events.WarpUseEvent
import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.Bukkit
import java.util.UUID

class WarpEventPublisherBukkit: WarpEventPublisher {
    override fun warpCreated(warp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpCreateEvent(warp))
    }

    override fun warpDeleted(warp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpDeleteEvent(warp))
    }

    override fun warpDiscovered(playerId: UUID, warpId: UUID) {
        Bukkit.getPluginManager().callEvent(WarpDiscoverEvent(playerId, warpId))
    }

    override fun warpModified(oldWarp: Warp, newWarp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpUpdateEvent(oldWarp, newWarp))
    }

    override fun warpTeleported(playerId: UUID, warp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpTeleportEvent(playerId, warp))
    }

    override fun warpUsed(playerId: UUID, warp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpUseEvent(playerId, warp))
    }
}
