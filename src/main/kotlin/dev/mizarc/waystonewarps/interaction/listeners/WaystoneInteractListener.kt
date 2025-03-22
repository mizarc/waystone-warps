package dev.mizarc.waystonewarps.interaction.listeners

import dev.mizarc.waystonewarps.application.actions.discovery.DiscoverWarp
import dev.mizarc.waystonewarps.application.actions.whitelist.GetWhitelistedPlayers
import dev.mizarc.waystonewarps.application.actions.world.GetWarpAtPosition
import dev.mizarc.waystonewarps.application.actions.world.IsValidWarpBase
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.management.WarpManagementMenu
import dev.mizarc.waystonewarps.interaction.menus.management.WarpNamingMenu
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import dev.mizarc.waystonewarps.interaction.messaging.AccentColourPalette
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
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

class WaystoneInteractListener(private val configService: ConfigService): Listener, KoinComponent {
    private val getWarpAtPosition: GetWarpAtPosition by inject()
    private val discoverWarp: DiscoverWarp by inject()
    private val getWhitelistedPlayers: GetWhitelistedPlayers by inject()
    private val isValidWarpBase: IsValidWarpBase by inject()

    @EventHandler
    fun onLodestoneInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val clickedBlock: Block = event.clickedBlock ?: return

        // Check for holding compass
        val itemInHand = event.player.inventory.itemInMainHand
        if (itemInHand.type == Material.COMPASS) return

        // Check for existing warp
        val warp = getWarpAtPosition.execute(clickedBlock.location.toPosition3D(), clickedBlock.world.uid)
        val menuNavigator = MenuNavigator(player)

        // Create new warp if not found, open management menu if owner, discover otherwise
        warp?.let {
            // Check if warp is locked and alert if no access
            event.isCancelled = true
            if (warp.isLocked && warp.playerId != player.uniqueId
                    && !getWhitelistedPlayers.execute(warp.id).contains(player.uniqueId)) {
                player.sendActionBar(Component.text("Warp is set to private").color(PrimaryColourPalette.FAILED.color))
                return
            }

            // Set location of particle spawn
            val particleLocation = clickedBlock.location.clone()
            particleLocation.x += 0.5
            particleLocation.y += 0.5
            particleLocation.z += 0.5

            if (it.playerId == player.uniqueId) {
                if (configService.allowWarpsMenuViaWaystone()) {
                    if (event.player.isSneaking) {
                        menuNavigator.openMenu(WarpManagementMenu(player, menuNavigator, it))
                    } else {
                        menuNavigator.openMenu(WarpMenu(player, menuNavigator))
                    }
                } else {
                    menuNavigator.openMenu(WarpManagementMenu(player, menuNavigator, it))
                }
            } else {
                val result = discoverWarp.execute(player.uniqueId, it.id)
                if (result) {
                    player.sendActionBar(Component.text("Warp ").color(PrimaryColourPalette.SUCCESS.color)
                        .append(Component.text(warp.name).color(AccentColourPalette.SUCCESS.color))
                        .append(Component.text( " has been discovered!").color(PrimaryColourPalette.SUCCESS.color)))
                    clickedBlock.world.spawnParticle(Particle.TOTEM_OF_UNDYING, particleLocation, 20)
                    clickedBlock.world.playSound(particleLocation, Sound.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f)
                } else {
                    if (configService.allowWarpsMenuViaWaystone()) {
                        menuNavigator.openMenu(WarpMenu(player, menuNavigator))
                    }
                    else {
                        player.sendActionBar(Component.text("Warp ").color(PrimaryColourPalette.INFO.color)
                            .append(Component.text(warp.name).color(AccentColourPalette.INFO.color))
                            .append(Component.text( " already discovered").color(PrimaryColourPalette.INFO.color)))
                    }
                }
            }
        }

        // Check if valid warp base to create warp
        val baseBlock = clickedBlock.getRelative(BlockFace.DOWN)
        if (isValidWarpBase.execute(baseBlock.type.toString())) {
            event.isCancelled = true
            menuNavigator.openMenu(WarpNamingMenu(player, menuNavigator, clickedBlock.location))
        }
    }
}