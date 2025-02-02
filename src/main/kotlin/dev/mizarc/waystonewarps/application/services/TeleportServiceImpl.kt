package dev.mizarc.waystonewarps.application.services

import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.waystonewarps.application.PlayerLimitService
import dev.mizarc.waystonewarps.application.TeleportService
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import dev.mizarc.waystonewarps.infrastructure.persistence.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TeleportServiceImpl(private val plugin: Plugin, private val config: Config,
                          private val playerLimitService: PlayerLimitService,
                          private val playerStateService: PlayerStateService
): TeleportService {
    private val activeTeleportations = ConcurrentHashMap<UUID, TaskHandle>()

    override fun teleportWaystone(player: Player, waystone: Waystone, sendAlert: Boolean) {
        val world = waystone.getWorld() ?: return
        val waystoneLocation = waystone.position.toLocation(world)
        waystoneLocation.x += 0.5
        waystoneLocation.y += 1
        waystoneLocation.z += 0.5

        // Player data
        val timer = playerLimitService.getTeleportTimer(player)
        val cost = playerLimitService.getTeleportCost(player)

        // Teleports the player instantaneously
        if (timer == 0) {
            removeCostFromInventory(player, cost)
            player.teleport(waystoneLocation)
            return
        }

        // Teleports the player after a certain amount of time has passed
        val playerState = playerStateService.getById(player.uniqueId) ?: return
        val player = playerState.getOnlinePlayer() ?: return
        val teleportTask = sche
        playerState.scheduleTeleport()

        playerState.teleportTask = TeleportTask(Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            // Check if player still has the cost required after the timer is up, prevents exploit by throwing items out
            if (!hasCostAmount(player, cost)) {
                player.sendActionBar(
                    Component
                        .text("Teleport cancelled. You no longer possess the required cost.")
                        .color(TextColor.color(255, 85, 85))
                )
            } else {
                removeCostFromInventory(player, cost)
                player.teleport((waystoneLocation.clone() as Location).apply {})
                player.sendActionBar(
                    Component
                        .text("Welcome to ${waystone.name}!")
                        .color(TextColor.color(85, 255, 85))
                )
            }
            playerState.teleportTask?.cancelTask()
        }, timer * 20L), player, location)
        return true
    }

    private fun hasCostAmount(player: Player, teleportCost: Int): Boolean {
        // Doesn't compile without non-null assertion for some reason. Don't remove it.
        val count = player.inventory.contents!!.sumOf { item ->
            if (item?.type == Material.ENDER_PEARL) item.amount else 0
        }
        return count >= teleportCost
    }

    private fun removeCostFromInventory(player: Player, teleportCost: Int) {
        var count = teleportCost
        // Doesn't compile without non-null assertion for some reason. Don't remove it.
        player.inventory.contents!!.forEach {
            if (it?.type == Material.ENDER_PEARL) {
                val remaining = player.inventory.removeItem(ItemStack(Material.ENDER_PEARL, teleportCost))
                count -= remaining[0]?.amount ?: teleportCost
                if (count <= 0) {
                    return
                }
            }
        }
    }
}