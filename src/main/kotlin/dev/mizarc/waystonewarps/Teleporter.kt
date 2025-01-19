package dev.mizarc.waystonewarps

import dev.mizarc.waystonewarps.domain.PlayerRepository
import dev.mizarc.waystonewarps.domain.Warp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import dev.mizarc.waystonewarps.utils.LocationConversions

class Teleporter(private val plugin: Plugin, private val config: Config, private val playerRepository: PlayerRepository) {

    fun teleportHome(player: Player): Boolean {
        val homeLocation = player.bedSpawnLocation ?: return false
        val playerState = playerRepository.getByPlayer(player) ?: return false

        // Do cost checks if required
        if (playerState.getHomeTeleportCost() > 1) {
            // Alert player that they can't teleport if they don't meet the cost
            if (!hasCostAmount(player, playerState.getHomeTeleportCost())) {
                player.sendActionBar(Component
                    .text("You require at least ${playerState.getHomeTeleportCost()} ender pearls to teleport home.")
                    .color(TextColor.color(255, 85, 85)))
                return false
            }

            // Teleport player home with cost
            player.sendActionBar(Component
                .text("Teleporting home. This will cost you ${playerState.getHomeTeleportCost()} ender pearls.")
                .color(TextColor.color(85, 255, 255)))
            teleport(player, homeLocation, playerState.getHomeTeleportTimer(),
                TeleportMessage.HOME, playerState.getHomeTeleportCost())
            return true
        }

        // Teleport player home without cost
        teleport(player, homeLocation, playerState.getHomeTeleportTimer())
        return true
    }

    fun teleportSpawn(player: Player): Boolean {
        val spawnLocation = LocationConversions.stringTolocation(config.spawnLocation) ?: return false
        val playerState = playerRepository.getByPlayer(player) ?: return false

        // Do cost checks if required
        if (playerState.getSpawnTeleportCost() > 1) {
            // Alert player that they can't teleport if they don't meet the cost
            if (!hasCostAmount(player, playerState.getSpawnTeleportCost())) {
                player.sendActionBar(Component
                    .text("You require at least ${playerState.getSpawnTeleportCost()} ender pearls to teleport to spawn.")
                    .color(TextColor.color(255, 85, 85)))
                return false
            }

            // Teleport player to spawn with cost
            player.sendActionBar(Component
                .text("Teleporting to spawn. This will cost you ${playerState.getSpawnTeleportCost()} ender pearls.")
                .color(TextColor.color(85, 255, 255)))
            teleport(player, spawnLocation, playerState.getSpawnTeleportTimer(),
                TeleportMessage.SPAWN, playerState.getSpawnTeleportCost())
            return true
        }

        // Teleport player to spawn without cost
        teleport(player, spawnLocation, playerState.getSpawnTeleportTimer())
        return true
    }

    fun teleportWarp(player: Player, warp: Warp): Boolean {
        val world = warp.getWorld() ?: return false
        val warpLocation = warp.position.toLocation(world)
        warpLocation.x += 0.5
        warpLocation.y += 1
        warpLocation.z += 0.5
        warpLocation.direction = Direction.toVector(warp.direction)
        val playerState = playerRepository.getByPlayer(player) ?: return false

        // Do cost checks if required
        if (playerState.getSpawnTeleportCost() > 1) {
            // Alert player that they can't teleport if they don't meet the cost
            if (!hasCostAmount(player, playerState.getWarpTeleportCost())) {
                player.sendActionBar(Component
                    .text("You require at least ${playerState.getWarpTeleportCost()} ender pearls to warp.")
                    .color(TextColor.color(255, 85, 85)))
                return false
            }

            // Teleport to warp with cost
            player.sendActionBar(Component
                .text("Warping to ${warp.name}. This will cost you ${playerState.getWarpTeleportCost()} ender pearls.")
                .color(TextColor.color(85, 255, 255)))
            teleport(player, warpLocation, playerState.getWarpTeleportTimer(),
                TeleportMessage.WARP, playerState.getWarpTeleportCost())
            return true
        }

        // Teleport to warp without cost
        teleport(player, warpLocation, playerState.getWarpTeleportCost())
        return true
    }

    fun teleport(player: Player, location: Location, timer: Int = 0,
                 teleportMessage: TeleportMessage = TeleportMessage.NONE, teleportCost: Int = 0): Boolean {
        val playerState = playerRepository.getByPlayer(player) ?: return false

        // Teleports the player instantaneously
        if (timer == 0) {
            removeCostFromInventory(player, teleportCost)
            player.teleport(location)
            return true
        }

        // Teleports the player after a certain amount of time has passed
        playerState.teleportTask = TeleportTask(Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (!hasCostAmount(player, teleportCost)) {
                player.sendActionBar(Component
                    .text("Teleport cancelled. You no longer possess the required cost.")
                    .color(TextColor.color(255, 85, 85)))
            }
            else {
                removeCostFromInventory(player, teleportCost)
                player.teleport((location))
                player.sendActionBar(Component
                    .text(teleportMessage.messageString)
                    .color(TextColor.color(85, 255, 85)))
            }
            playerState.teleportTask?.cancelTask()
        }, timer * 20L), playerState.player, location)
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