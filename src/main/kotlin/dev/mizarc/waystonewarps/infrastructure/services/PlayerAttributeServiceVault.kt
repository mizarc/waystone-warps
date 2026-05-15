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

    override fun getTeleportCost(playerId: UUID): Double {
        PermissionWarpCost.get(playerId)?.let { return it }

        val vaultCost = metadata.getPlayerInfoDouble(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_cost", configService.getTeleportCostAmount()
            ).takeIf { it > -1.0 } ?: -1.0
            
        // Check if Vault metadata has free value (-1 is treated as free)
        if (vaultCost == -1.0) {
            return 0.0
        }
        
        return vaultCost
    }

    override fun getTeleportTimer(playerId: UUID): Int {
        PermissionWarpTimer.get(playerId)?.let { return it }

        val vaultTimer = metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_timer", configService.getTeleportTimer())
            .takeIf { it > -1 } ?: -1

        // Check if Vault metadata has instant value (-1 is treated as instant)
        if (vaultTimer == -1) {
            return 0
        }

        return vaultTimer
    }

    override fun getTeleportCooldown(playerId: UUID): Int {
        PermissionWarpCooldown.get(playerId)?.let { return it }

        val vaultCooldown = metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getPlayer(playerId),
            "waystonewarps.teleport_cooldown", configService.getTeleportCooldown())
            .takeIf { it > -1 } ?: -1

        // Check if Vault metadata has no cooldown value (-1 is treated as no cooldown)
        if (vaultCooldown == -1) {
            return 0
        }

        return vaultCooldown
    }
}
