package dev.mizarc.waystonewarps.interaction.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import dev.mizarc.waystonewarps.*
import dev.mizarc.waystonewarps.infrastructure.persistence.warps.WarpRepositorySQLite
import dev.mizarc.waystonewarps.interaction.menus.WarpManagementMenu

class WarpInteractListener(var warpRepositorySQLite: WarpRepositorySQLite,
                           var warpAccessRepository: WarpAccessRepository
): Listener {

    @EventHandler
    fun onPlayerWarpInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if ((event.clickedBlock?.type ?: return) != Material.LODESTONE) return

        val existingWarp = waystoneRepositorySQLite.getAll().find { it.position == Position(event.clickedBlock!!.location) }
        if (event.player.isSneaking) {
            if (!event.player.hasPermission("worldwidewarps.action.warp_manage")) return
            val warpBuilder = Warp.Builder(event.player, event.clickedBlock!!.location.world, Position(event.clickedBlock!!.location))
            WarpManagementMenu(waystoneRepositorySQLite, warpAccessRepository, warpBuilder).openWarpManagementMenu()
        }
        if (existingWarp == null) return

        if (!event.player.hasPermission("worldwidewarps.action.warp_unlock")) return
        if (!warpAccessRepository.hasAccess(event.player, existingWarp)) {
            warpAccessRepository.addWarpForPlayer(event.player, existingWarp)
            event.player.sendActionBar(
                Component
                    .text("Warp '${existingWarp.name}' Unlocked")
                    .color(TextColor.color(85, 255, 85)))
        }
    }
}