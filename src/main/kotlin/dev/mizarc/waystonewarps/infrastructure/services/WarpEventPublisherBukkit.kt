package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.api.events.WarpCreateEvent
import dev.mizarc.waystonewarps.api.events.WarpDeleteEvent
import dev.mizarc.waystonewarps.api.events.WarpUpdateEvent
import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.warps.Warp
import org.bukkit.Bukkit

class WarpEventPublisherBukkit: WarpEventPublisher {
    override fun warpCreated(warp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpCreateEvent(warp))
    }

    override fun warpDeleted(warp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpDeleteEvent(warp))
    }

    override fun warpModified(oldWarp: Warp, newWarp: Warp) {
        Bukkit.getPluginManager().callEvent(WarpUpdateEvent(oldWarp, newWarp))
    }
}