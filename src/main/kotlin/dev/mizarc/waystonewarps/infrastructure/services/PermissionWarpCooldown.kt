package dev.mizarc.waystonewarps.infrastructure.services

import org.bukkit.Bukkit
import java.util.UUID

object PermissionWarpCooldown {
    private val warpCooldownPermission = Regex("""^waystonewarps.teleport_cooldown\.(\d+)$""", RegexOption.IGNORE_CASE)
    private val noCooldownPermission = "waystonewarps.teleport_cooldown.*"

    fun get(playerId: UUID): Int? {
        val player = Bukkit.getPlayer(playerId) ?: return null

        // Check for no cooldown permission first
        if (player.hasPermission(noCooldownPermission)) {
            return 0
        }

        return player.effectivePermissions
            .asSequence()
            .filter { it.value }
            .mapNotNull { permission ->
                warpCooldownPermission.matchEntire(permission.permission)?.groupValues?.get(1)?.toIntOrNull()
            }
            .minOrNull() // Lowest cooldown wins
    }
}
