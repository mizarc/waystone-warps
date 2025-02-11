package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import java.util.*

/**
 * A Vault API player attribute implementation that handles read only player limits.
 *
 * This implementation uses the Vault API to store limits via a third party provider (e.g. LuckPerms)
 */
class PlayerAttributeServiceVault(private val configService: ConfigService,
                                  private val metadata: Chat): PlayerAttributeService {
    override fun getWarpLimit(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.warp_limit", configService.getWarpLimit())
            .takeIf { it > -1 } ?: -1

    override fun getTeleportCost(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_cost", configService.getTeleportCostAmount())
            .takeIf { it > -1 } ?: -1

    override fun getTeleportTimer(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_timer", configService.getTeleportTimer())
            .takeIf { it > -1 } ?: -1
}