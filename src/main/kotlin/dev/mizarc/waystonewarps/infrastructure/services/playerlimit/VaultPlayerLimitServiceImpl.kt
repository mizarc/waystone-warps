package dev.mizarc.waystonewarps.infrastructure.services.playerlimit

import dev.mizarc.waystonewarps.Config
import dev.mizarc.waystonewarps.TeleportTask
import dev.mizarc.waystonewarps.api.PlayerLimitService
import net.milkbowl.vault.chat.Chat
import org.bukkit.entity.Player

class VaultPlayerLimitServiceImpl(val player: Player, private val config: Config,
                                  private val metadata: Chat): PlayerLimitService {
    var teleportTask: TeleportTask? = null

    override fun getWaystoneLimit(): Int =
        metadata.getPlayerInfoInteger(player, "waystonewarps.waystone_limit", config.waystoneLimit)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportCost(): Int =
        metadata.getPlayerInfoInteger(player, "waystonewarps.teleport_cost", config.teleportCost)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportTimer(): Int =
        metadata.getPlayerInfoInteger(player, "waystonewarps.teleport_timer", config.teleportTimer)
            .takeIf { it > -1 } ?: -1
}