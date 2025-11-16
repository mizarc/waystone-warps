package dev.mizarc.waystonewarps.interaction.utils

import org.bukkit.entity.Player
import java.util.UUID

object PermissionHelper {
    /**
     * Checks if a player can modify a waystone (either they own it or have admin permission)
     */
    fun canModifyWaystone(player: Player, waystoneOwnerId: UUID, adminPermission: String): Boolean {
        // Players can always modify their own waystones
        if (player.uniqueId == waystoneOwnerId) {
            return true
        }

        // Check if player has the specific admin permission
        return player.hasPermission(adminPermission)
    }

    /**
     * Checks if a player can change access control (public/private) on a waystone
     */
    fun canChangeAccessControl(player: Player, waystoneOwnerId: UUID): Boolean {
        return canModifyWaystone(player, waystoneOwnerId, "waystones.admin.accesscontrol")
    }

    /**
     * Checks if a player can manage the whitelist on a waystone
     */
    fun canManageWhitelist(player: Player, waystoneOwnerId: UUID): Boolean {
        return canModifyWaystone(player, waystoneOwnerId, "waystones.admin.manage")
    }

    /**
     * Checks if a player can rename a waystone
     */
    fun canRename(player: Player, waystoneOwnerId: UUID): Boolean {
        return canModifyWaystone(player, waystoneOwnerId, "waystones.admin.rename")
    }

    /**
     * Checks if a player can change the icon of a waystone
     */
    fun canChangeIcon(player: Player, waystoneOwnerId: UUID): Boolean {
        return canModifyWaystone(player, waystoneOwnerId, "waystones.admin.icon")
    }

    /**
     * Checks if a player can relocate a waystone
     */
    fun canRelocate(player: Player, waystoneOwnerId: UUID): Boolean {
        return canModifyWaystone(player, waystoneOwnerId, "waystones.admin.relocate")
    }
}
