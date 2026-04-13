package dev.mizarc.waystonewarps.infrastructure.services

import org.bukkit.Bukkit
import java.util.UUID

object PermissionWarpLimit {
    private val warpLimitPermission = Regex("""^waystone\.(\d+)$""", RegexOption.IGNORE_CASE)

    fun get(playerId: UUID): Int? {
        val player = Bukkit.getPlayer(playerId) ?: return null
        return player.effectivePermissions
            .asSequence()
            .filter { it.value }
            .mapNotNull { permission ->
                warpLimitPermission.matchEntire(permission.permission)?.groupValues?.get(1)?.toIntOrNull()
            }
            .maxOrNull()
    }
}
