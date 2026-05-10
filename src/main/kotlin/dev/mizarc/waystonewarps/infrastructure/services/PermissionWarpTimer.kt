package dev.mizarc.waystonewarps.infrastructure.services

import org.bukkit.Bukkit
import java.util.UUID

object PermissionWarpTimer {
    private val warpTimerPermission = Regex("""^waystonewarps.teleport_timer\.(\d+)$""", RegexOption.IGNORE_CASE)
    private val instantPermission = "waystonewarps.teleport_timer.*"

    fun get(playerId: UUID): Int? {
        val player = Bukkit.getPlayer(playerId) ?: return null

        // Check for instant permission first
        if (player.hasPermission(instantPermission)) {
            return 0
        }

        return player.effectivePermissions
            .asSequence()
            .filter { it.value }
            .mapNotNull { permission ->
                warpTimerPermission.matchEntire(permission.permission)?.groupValues?.get(1)?.toIntOrNull()
            }
            .minOrNull() // Lowest timer wins
    }
}
