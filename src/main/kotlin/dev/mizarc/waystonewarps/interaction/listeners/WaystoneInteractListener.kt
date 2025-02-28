package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.discovery.DiscoverWarp
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.management.WarpManagementMenu
import dev.mizarc.waystonewarps.interaction.menus.management.WarpNamingMenu
import dev.mizarc.waystonewarps.interaction.messaging.AccentColourPalette
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaystoneInteractListener: Listener, KoinComponent {
    private val getWarpAtPosition: GetWarpAtPosition by inject()
    private val discoverWarp: DiscoverWarp by inject()
    private val getWhitelistedPlayers: GetWhitelistedPlayers by inject()

    @EventHandler
    fun onLodestoneInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val clickedBlock: Block = event.clickedBlock ?: return

        // Check for right click lodestone
        if (event.action != Action.RIGHT_CLICK_BLOCK || clickedBlock.type != Material.LODESTONE) return

        // Check for holding compass
        val itemInHand = event.player.inventory.itemInMainHand
        if (itemInHand.type == Material.COMPASS) return

        // Check for smooth stone or barrier below lodestone
        val blockBelow: Block = clickedBlock.getRelative(BlockFace.DOWN)
        if (blockBelow.type != Material.SMOOTH_STONE && blockBelow.type != Material.BARRIER) return

        // Check for existing warp
        val warp = getWarpAtPosition.execute(clickedBlock.location.toPosition3D(), clickedBlock.world.uid)
        val menuNavigator = MenuNavigator(player)

        // Create new warp if not found, open management menu if owner, discover otherwise
        event.isCancelled = true
        warp?.let {
            // Check if warp is locked and alert if no access
            if (warp.isLocked && warp.playerId != player.uniqueId
                    && !getWhitelistedPlayers.execute(warp.id).contains(player.uniqueId)) {
                player.sendActionBar(Component.text("Warp is set to private").color(PrimaryColourPalette.FAILED.color))
                return
            }

            if (it.playerId == player.uniqueId) {
                menuNavigator.openMenu(WarpManagementMenu(player, menuNavigator, it))
            } else {
                val result = discoverWarp.execute(player.uniqueId, it.id)
                if (result) {
                    player.sendActionBar(Component.text("Warp ").color(PrimaryColourPalette.SUCCESS.color)
                        .append(Component.text(warp.name).color(AccentColourPalette.SUCCESS.color))
                        .append(Component.text( " has been discovered!").color(PrimaryColourPalette.SUCCESS.color)))
                } else {
                    player.sendActionBar(Component.text("Warp ").color(PrimaryColourPalette.INFO.color)
                        .append(Component.text(warp.name).color(AccentColourPalette.INFO.color))
                        .append(Component.text( " already discovered").color(PrimaryColourPalette.INFO.color)))
                }
            }
        } ?: menuNavigator.openMenu(WarpNamingMenu(player, menuNavigator, clickedBlock.location))
    }
}