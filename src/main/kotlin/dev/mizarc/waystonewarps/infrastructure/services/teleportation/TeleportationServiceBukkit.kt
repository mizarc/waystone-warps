package dev.mizarc.waystonewarps.infrastructure.services.teleportation

import dev.mizarc.waystonewarps.application.results.TeleportResult
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.MovementMonitorService
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.application.services.scheduling.SchedulerService
import dev.mizarc.waystonewarps.application.services.scheduling.Task
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TeleportationServiceBukkit(private val playerAttributeService: PlayerAttributeService,
                                 private val configService: ConfigService,
                                 private val movementMonitorService: MovementMonitorService,
                                 private val whitelistRepository: WhitelistRepository,
                                 private val scheduler: SchedulerService,
                                 private val economy: Economy?): TeleportationService {
    private val activeTeleportations = ConcurrentHashMap<UUID, PendingTeleport>()

    override fun teleportPlayer(playerId: UUID, warp: Warp): TeleportResult {
        // Player data
        val player = Bukkit.getPlayer(playerId) ?: return TeleportResult.FAILED

        // Check for cost
        val result = hasCost(player)
        if (!result) return TeleportResult.INSUFFICIENT_FUNDS

        // Check for lock
        if (warp.isLocked && warp.playerId != playerId && !whitelistRepository.isWhitelisted(warp.id, playerId))
            return TeleportResult.LOCKED

        // Location data
        val world = Bukkit.getWorld(warp.worldId) ?: return TeleportResult.WORLD_NOT_FOUND
        val warpLocation = warp.position.toLocation(world)
        warpLocation.x += 0.5
        warpLocation.y += 1
        warpLocation.z += 0.5

        // Generate offset location based on player pitch and yaw
        val offsetLocation = getOffsetLocation(warpLocation, player.yaw)
        offsetLocation.pitch = player.pitch
        offsetLocation.yaw = player.yaw
        offsetLocation.add(0.0, -2.0, 0.0)

        // Teleports the player instantaneously
        deductCost(player)
        clearArea(warp.position.toLocation(world))
        buildPlatform(warp.position.toLocation(world))
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 200, 4, false, false))
        player.teleport(offsetLocation)
        return TeleportResult.SUCCESS
    }

    override fun scheduleDelayedTeleport(playerId: UUID, warp: Warp, delaySeconds: Int, onSuccess: () -> Unit,
                                         onPending: () -> Unit, onInsufficientFunds: () -> Unit, onCanceled: () -> Unit,
                                         onWorldNotFound: () -> Unit, onLocked: () -> Unit, onFailure: () -> Unit) {
        // Cancel existing pending teleport if any
        activeTeleportations[playerId]?.let {
            it.taskHandle.cancel()
            movementMonitorService.stopMonitoringPlayer(playerId)
            it.onCanceled()
        }

        // Get player object
        val player = Bukkit.getPlayer(playerId)
        if (player == null) {
            onFailure()
            return
        }

        // Cancel if player doesn't have the funds to teleport
        val result = hasCost(player)
        if (!result) {
            onInsufficientFunds()
            return
        }

        // Cancel if locked
        if (warp.isLocked && warp.playerId != playerId && !whitelistRepository.isWhitelisted(warp.id, playerId)) {
            onLocked()
            return
        }

        // Instant teleport if player doesn't have a teleport timer
        if (playerAttributeService.getTeleportTimer(playerId) <= 0) {
            teleportPlayer(playerId, warp)
            onSuccess()
            return
        }

        // Schedule the new teleportation task
        val taskHandle = scheduler.schedule(delaySeconds * 20L) {
            movementMonitorService.stopMonitoringPlayer(playerId)
            activeTeleportations.remove(playerId)
            val teleportResult = teleportPlayer(playerId, warp)
            when (teleportResult) {
                TeleportResult.SUCCESS -> onSuccess()
                TeleportResult.INSUFFICIENT_FUNDS -> onInsufficientFunds()
                TeleportResult.WORLD_NOT_FOUND -> onWorldNotFound()
                TeleportResult.LOCKED -> onLocked()
                TeleportResult.FAILED -> onFailure()
            }
        }

        // Monitor for player movement
        movementMonitorService.monitorPlayerMovement(playerId) {
            taskHandle.cancel()
            movementMonitorService.stopMonitoringPlayer(playerId)
            activeTeleportations.remove(playerId)
            onCanceled()
        }

        // Store the pending teleport
        activeTeleportations[playerId] = PendingTeleport(taskHandle, onCanceled)
        onPending()
    }

    override fun cancelPendingTeleport(playerId: UUID): Result<Unit> {
        val pendingTeleport = activeTeleportations.remove(playerId)
            ?: return Result.failure(Exception("No pending teleport to cancel."))
        movementMonitorService.stopMonitoringPlayer(playerId)
        pendingTeleport.taskHandle.cancel()
        pendingTeleport.onCanceled()
        return Result.success(Unit)
    }

    private fun hasCost(player: Player): Boolean {
        val teleportCost = playerAttributeService.getTeleportCost(player.uniqueId)
        return when (configService.getTeleportCostType()) {
            CostType.ITEM -> hasEnoughItems(player, Material.valueOf(configService.getTeleportCostItem()), teleportCost)
            CostType.MONEY -> hasEnoughMoney(player, teleportCost)
            CostType.XP -> hasEnoughXp(player, teleportCost)
        }
    }

    private fun deductCost(player: Player) {
        val teleportCost = playerAttributeService.getTeleportCost(player.uniqueId)
        when (configService.getTeleportCostType()) {
            CostType.ITEM -> removeItems(player, Material.valueOf(configService.getTeleportCostItem()), teleportCost)
            CostType.MONEY -> subtractMoney(player, teleportCost)
            CostType.XP -> subtractXp(player, teleportCost)
        }
    }

    private fun hasEnoughItems(player: Player, itemMaterial: Material, teleportCost: Double): Boolean {
        var count = teleportCost.toInt()
        for (item in player.inventory.contents.filterNotNull()) {
            if (item.type == itemMaterial) {
                val removeAmount = minOf(item.amount, count)
                count -= removeAmount
                if (count <= 0) {
                    return true
                }
            }
        }
        return false
    }

    private fun removeItems(player: Player, itemMaterial: Material, teleportCost: Double) {
        var count = teleportCost.toInt()
        for (item in player.inventory.contents.filterNotNull()) {
            if (item.type == itemMaterial) {
                val removeAmount = minOf(item.amount, count)
                item.amount -= removeAmount
                count -= removeAmount
                if (count <= 0) {
                    break
                }
            }
        }
    }

    private fun hasEnoughMoney(player: Player, teleportCost: Double): Boolean {
        return economy?.has(player, teleportCost) == true
    }

    private fun subtractMoney(player: Player, teleportCost: Double) {
        economy?.withdrawPlayer(player, teleportCost.toDouble())
    }

    private fun hasEnoughXp(player: Player, teleportCost: Double): Boolean {
        return player.level >= teleportCost
    }

    private fun subtractXp(player: Player, teleportCost: Double) {
        player.giveExpLevels(-teleportCost.toInt())
    }

    private fun getOffsetLocation(location: Location, playerYaw: Float): Location {
        return if (playerYaw >= -22.5 && playerYaw < 22.5) location.add(0.0, 0.0, 1.0) // SOUTH
        else if (playerYaw >= 22.5 && playerYaw < 67.5) location.add(-1.0, 0.0, 1.0) // SOUTH_EAST
        else if (playerYaw >= 67.5 && playerYaw < 112.5) location.add(-1.0, 0.0, 0.0) // EAST
        else if (playerYaw >= 112.5 && playerYaw < 157.5) location.add(-1.0, 0.0, -1.0) // NORTH_EAST
        else if (playerYaw >= 157.5 || playerYaw < -157.5) location.add(0.0, 0.0, -1.0) // NORTH
        else if (playerYaw >= -157.5 && playerYaw < -112.5) location.add(1.0, 0.0, -1.0) // NORTH_WEST
        else if (playerYaw >= -112.5 && playerYaw < -67.5) location.add(1.0, 0.0, 0.0) // WEST
        else location.add(1.0, 0.0, 1.0)
    }


    private data class PendingTeleport(
        val taskHandle: Task,
        val onCanceled: () -> Unit
    )

    private fun clearArea(location: Location) {
        // Loop through the blocks in the specified range
        for (x in -1..1) {
            for (y in -1..0) { // Only 2 blocks tall
                for (z in -1..1) {
                    // Skip the center block itself
                    if ((x == 0 && y == 0 && z == 0) || (x == 0 && y == -1 && z == 0)) continue

                    val block = location.world.getBlockAt(location.blockX + x, location.blockY + y, location.blockZ + z)

                    // Break the block and drop its items naturally
                    if (!block.type.isAir) {
                        block.breakNaturally()
                    }
                }
            }
        }
    }

    private fun buildPlatform(location: Location) {
        for (x in -1..1) {
            for (z in -1..1) {
                val block = location.world.getBlockAt(location.blockX + x, location.blockY - 2, location.blockZ + z)

                if (block.type in configService.getPlatformReplaceBlocks().mapNotNull { it ->
                        runCatching { Material.valueOf(it) }.getOrNull() }) {
                    block.breakNaturally()
                    block.type = Material.COBBLESTONE
                }
            }
        }
    }
}