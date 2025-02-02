package dev.mizarc.waystonewarps.infrastructure.persistence.players

import dev.mizarc.waystonewarps.domain.players.PlayerLimitRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.Config
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PlayerLimitRepositoryVault(private val config: Config,
                                 private val metadata: Chat): PlayerLimitRepository {
    override fun getWaystoneLimit(player: OfflinePlayer): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, player,
            "waystonewarps.waystone_limit", config.waystoneLimit)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportCost(player: OfflinePlayer): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, player,
            "waystonewarps.teleport_cost", config.teleportCost)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportTimer(player: OfflinePlayer): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, player,
            "waystonewarps.teleport_timer", config.teleportTimer)
            .takeIf { it > -1 } ?: -1
}