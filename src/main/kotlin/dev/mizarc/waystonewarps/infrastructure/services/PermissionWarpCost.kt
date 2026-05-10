package dev.mizarc.waystonewarps.infrastructure.services

import org.bukkit.Bukkit
import java.util.UUID

object PermissionWarpCost {
    private val warpCostPermission = Regex("""^waystonewarps.teleport_cost\.(\d+(?:\.\d+)?)$""", RegexOption.IGNORE_CASE)
    private val freePermission = "waystonewarps.teleport_cost.*"

    fun get(playerId: UUID): Double? {
        val player = Bukkit.getPlayer(playerId) ?: return null

        // Check for free permission first
        if (player.hasPermission(freePermission)) {
            return 0.0
        }

        return player.effectivePermissions
            .asSequence()
            .filter { it.value }
            .mapNotNull { permission ->
                warpCostPermission.matchEntire(permission.permission)?.groupValues?.get(1)?.toDoubleOrNull()
            }
            .minOrNull() // Lowest cost wins
    }
}
