package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.domain.world.WorldService
import org.bukkit.Bukkit
import java.util.UUID

/**
 * Bukkit implementation of WorldService.
 */
class WorldServiceBukkit : WorldService {
    override fun isWorldInvalid(worldId: UUID): Boolean {
        return Bukkit.getWorld(worldId) == null
    }

    override fun isWorldInvalid(worldName: String): Boolean {
        return Bukkit.getWorld(worldName) == null
    }
}
