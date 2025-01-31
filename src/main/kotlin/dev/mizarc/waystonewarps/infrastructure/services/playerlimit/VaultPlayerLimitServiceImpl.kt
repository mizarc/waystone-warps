package dev.mizarc.waystonewarps.infrastructure.services.playerlimit

import dev.mizarc.waystonewarps.infrastructure.persistence.Config
import dev.mizarc.waystonewarps.api.PlayerLimitService
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class VaultPlayerLimitServiceImpl(private val config: Config, private val metadata: Chat): PlayerLimitService {
    override fun getWaystoneLimit(player: OfflinePlayer): Int =
        metadata.getPlayerInfoInteger(Bukkit.getServer().worlds[0].name, player,
            "waystonewarps.waystone_limit", config.waystoneLimit)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportCost(player: OfflinePlayer): Int =
        metadata.getPlayerInfoInteger(Bukkit.getServer().worlds[0].name, player,
            "waystonewarps.teleport_cost", config.teleportCost)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportTimer(player: OfflinePlayer): Int =
        metadata.getPlayerInfoInteger(Bukkit.getServer().worlds[0].name, player,
            "waystonewarps.teleport_timer", config.teleportTimer)
            .takeIf { it > -1 } ?: -1
}