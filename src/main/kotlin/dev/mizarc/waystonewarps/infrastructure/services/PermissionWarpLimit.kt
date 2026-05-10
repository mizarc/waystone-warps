package dev.mizarc.waystonewarps.infrastructure.services

import org.bukkit.Bukkit
import java.util.UUID

object PermissionWarpLimit {
    private val warpLimitPermission = Regex("""^waystonewarps.warp_limit\.(\d+)$""", RegexOption.IGNORE_CASE)
    private val unlimitedPermission = "waystonewarps.warp_limit.*"

    fun get(playerId: UUID): Int? {
        val player = Bukkit.getPlayer(playerId) ?: return null
        
        // Check for unlimited permission first
        if (player.hasPermission(unlimitedPermission)) {
            return Int.MAX_VALUE
        }
        
        return player.effectivePermissions
            .asSequence()
            .filter { it.value }
            .mapNotNull { permission ->
                warpLimitPermission.matchEntire(permission.permission)?.groupValues?.get(1)?.toIntOrNull()
            }
            .maxOrNull()
    }
}
