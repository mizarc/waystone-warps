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
    override fun getWarpLimit(playerId: UUID): Int {
        PermissionWarpLimit.get(playerId)?.let { return it }

        val vaultLimit = metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.warp_limit", configService.getWarpLimit())
            .takeIf { it > -1 } ?: -1
            
        // Check if Vault metadata has unlimited value (-1 is treated as unlimited)
        if (vaultLimit == -1) {
            return Int.MAX_VALUE
        }
        
        return vaultLimit
    }

    override fun getTeleportCost(playerId: UUID): Double =
        metadata.getPlayerInfoDouble(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_cost", configService.getTeleportCostAmount()
            ).takeIf { it > -1.0 } ?: -1.0

    override fun getTeleportTimer(playerId: UUID): Int =
        metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_timer", configService.getTeleportTimer())
            .takeIf { it > -1 } ?: -1
}
