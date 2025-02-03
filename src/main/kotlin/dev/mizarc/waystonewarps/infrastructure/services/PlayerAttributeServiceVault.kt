package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.infrastructure.persistence.Config
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import java.util.*

/**
 * A Vault API player attribute implementation that handles read only player limits.
 *
 * This implementation uses the Vault API to store limits via a third party provider (e.g. LuckPerms)
 */
class PlayerAttributeServiceVault(private val config: Config,
                                  private val metadata: Chat): PlayerAttributeService {
    override fun getWaystoneLimit(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.waystone_limit", config.waystoneLimit)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportCost(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_cost", config.teleportCost)
            .takeIf { it > -1 } ?: -1

    override fun getTeleportTimer(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_timer", config.teleportTimer)
            .takeIf { it > -1 } ?: -1
}