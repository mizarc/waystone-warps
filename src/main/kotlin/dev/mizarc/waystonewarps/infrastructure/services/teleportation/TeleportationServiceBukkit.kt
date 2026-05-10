package dev.mizarc.waystonewarps.infrastructure.services.teleportation

import dev.mizarc.waystonewarps.application.results.TeleportResult
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.MovementMonitorService
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import dev.mizarc.waystonewarps.application.services.TeleportationService
import dev.mizarc.waystonewarps.application.services.TownyService
import dev.mizarc.waystonewarps.application.services.WorldGroupService
import dev.mizarc.waystonewarps.application.services.scheduling.SchedulerService
import dev.mizarc.waystonewarps.application.services.scheduling.Task
import dev.mizarc.waystonewarps.domain.playerstate.PlayerState
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpAccess
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TeleportationServiceBukkit(private val playerAttributeService: PlayerAttributeService,
                                 private val configService: ConfigService,
                                 private val movementMonitorService: MovementMonitorService,
                                 private val whitelistRepository: WhitelistRepository,
                                 private val playerStateRepository: PlayerStateRepository,
                                 private val scheduler: SchedulerService,
                                 private val economy: Economy?,
                                 private val townyService: TownyService?,
                                 private val worldGroupService: WorldGroupService?): TeleportationService {
    private val activeTeleportations = ConcurrentHashMap<UUID, PendingTeleport>()

    override fun teleportPlayer(playerId: UUID, warp: Warp): TeleportResult {
        // Player data
        val player = Bukkit.getPlayer(playerId) ?: return TeleportResult.FAILED

        // Check base teleport permission
        if (!player.hasPermission("waystonewarps.teleport")) {
            return TeleportResult.PERMISSION_DENIED
        }

        // Check cooldown
        if (!player.hasPermission("waystonewarps.teleport.cooldown_bypass")) {
            val cooldownMs = configService.getTeleportCooldown() * 1000L
            val lastTeleport = getPlayerState(playerId).lastTeleportTime
            if (lastTeleport > 0L && System.currentTimeMillis() - lastTeleport < cooldownMs) {
                return TeleportResult.ON_COOLDOWN
            }
        }

        // Check inter-world/inter-world-group teleport permission if changing worlds
        val currentWorld = player.world.uid
        var hasCrossWorld = false

        if (warp.worldId == currentWorld) {
            hasCrossWorld = true
        } else if (
            worldGroupService != null &&
            worldGroupService.inSameGroup(
                warp.worldId,
                currentWorld
            ) &&
            player.hasPermission("waystonewarps.teleport.interworldgroup")
        ) {
            hasCrossWorld = true
        } else if (warp.worldId != currentWorld && player.hasPermission("waystonewarps.teleport.interworld")) {
            hasCrossWorld = true
        }
        if (!hasCrossWorld) {
            return TeleportResult.INTERWORLD_PERMISSION_DENIED
        }

        // Check for cost
        val result = hasCost(player, warp)
        if (!result) return TeleportResult.INSUFFICIENT_FUNDS

        // Check for lock
        if (warp.accessLevel == WarpAccess.PRIVATE && warp.playerId != playerId && !whitelistRepository.isWhitelisted(warp.id, playerId)
                && !player.hasPermission("waystonewarps.bypass.private_access"))
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
        deductCost(player, warp)
        clearArea(warp.position.toLocation(world), player.yaw)
        buildPlatform(warp.position.toLocation(world))
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 200, 4, false, false))
        player.teleport(offsetLocation)
        getPlayerState(playerId).lastTeleportTime = System.currentTimeMillis()
        return TeleportResult.SUCCESS
    }

    override fun scheduleDelayedTeleport(playerId: UUID, warp: Warp, delaySeconds: Int, onSuccess: () -> Unit,
                                         onPending: () -> Unit, onInsufficientFunds: () -> Unit, onCanceled: () -> Unit,
                                         onWorldNotFound: () -> Unit, onLocked: () -> Unit, onFailure: () -> Unit,
                                         onPermissionDenied: () -> Unit, onInterworldPermissionDenied: () -> Unit,
                                         onCooldown: (secondsRemaining: Int) -> Unit) {
        // Cancel existing pending teleport if any
        activeTeleportations[playerId]?.let {
            it.taskHandle.cancel()
            movementMonitorService.stopMonitoringPlayer(playerId)
            getPlayerState(playerId).isTeleporting = false
            it.onCanceled()
        }

        // Get player object
        val player = Bukkit.getPlayer(playerId)
        if (player == null) {
            onFailure()
            return
        }

        // Cancel if on cooldown
        if (!player.hasPermission("waystonewarps.teleport.cooldown_bypass")) {
            val cooldownMs = configService.getTeleportCooldown() * 1000L
            val lastTeleport = getPlayerState(playerId).lastTeleportTime
            if (lastTeleport > 0L && System.currentTimeMillis() - lastTeleport < cooldownMs) {
                val secondsRemaining = ((cooldownMs - (System.currentTimeMillis() - lastTeleport)) / 1000 + 1).toInt()
                onCooldown(secondsRemaining)
                return
            }
        }

        // Cancel if player doesn't have the funds to teleport
        val result = hasCost(player, warp)
        if (!result) {
            onInsufficientFunds()
            return
        }

        // Cancel if locked
        if (warp.accessLevel == WarpAccess.PRIVATE && warp.playerId != playerId && !whitelistRepository.isWhitelisted(warp.id, playerId)
                && !player.hasPermission("waystonewarps.bypass.private_access")) {
            onLocked()
            return
        }

        // Check for cooldown bypass or instant teleport if no timer
        if (player.hasPermission("waystonewarps.teleport.cooldown_bypass")
                || playerAttributeService.getTeleportTimer(playerId) <= 0) {
            val teleportResult = teleportPlayer(playerId, warp)
            if (teleportResult == TeleportResult.SUCCESS) {
                onSuccess()
            } else {
                // Handle other teleport results if needed
                when (teleportResult) {
                    TeleportResult.INSUFFICIENT_FUNDS -> onInsufficientFunds()
                    TeleportResult.WORLD_NOT_FOUND -> onWorldNotFound()
                    TeleportResult.LOCKED -> onLocked()
                    TeleportResult.FAILED -> onFailure()
                    TeleportResult.PERMISSION_DENIED -> onPermissionDenied()
                    TeleportResult.INTERWORLD_PERMISSION_DENIED -> onInterworldPermissionDenied()
                    TeleportResult.ON_COOLDOWN -> onFailure()
                }
            }
            return
        }

        // Schedule the new teleportation task
        getPlayerState(playerId).isTeleporting = true
        val taskHandle = scheduler.schedule(delaySeconds * 20L) {
            movementMonitorService.stopMonitoringPlayer(playerId)
            activeTeleportations.remove(playerId)
            getPlayerState(playerId).isTeleporting = false
            val teleportResult = teleportPlayer(playerId, warp)
            when (teleportResult) {
                TeleportResult.SUCCESS -> onSuccess()
                TeleportResult.INSUFFICIENT_FUNDS -> onInsufficientFunds()
                TeleportResult.WORLD_NOT_FOUND -> onWorldNotFound()
                TeleportResult.LOCKED -> onLocked()
                TeleportResult.FAILED -> onFailure()
                TeleportResult.PERMISSION_DENIED -> onPermissionDenied()
                TeleportResult.INTERWORLD_PERMISSION_DENIED -> onInterworldPermissionDenied()
                TeleportResult.ON_COOLDOWN -> onFailure()
            }
        }

        // Monitor for player movement
        movementMonitorService.monitorPlayerMovement(playerId) {
            taskHandle.cancel()
            movementMonitorService.stopMonitoringPlayer(playerId)
            activeTeleportations.remove(playerId)
            getPlayerState(playerId).isTeleporting = false
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
        getPlayerState(playerId).isTeleporting = false
        pendingTeleport.onCanceled()
        return Result.success(Unit)
    }

    override fun calculateCost(playerId: UUID, warp: Warp): Int {
        if (!configService.isTeleportCostEnabled()) return 0
        val player = Bukkit.getPlayer(playerId) ?: return configService.getTeleportCostMax()
        if (player.hasPermission("waystonewarps.bypass.cost")) return 0
        return calculateCostInternal(player, warp).toInt()
    }

    private fun calculateCostInternal(player: Player, warp: Warp): Double {
        val baseCost = playerAttributeService.getTeleportCost(player.uniqueId)

        // Same-town travel is free (same world only; cross-world towns are not considered)
        if (townyService != null && player.world.uid == warp.worldId) {
            val warpLocation = warp.position.toLocation(player.world)
            if (townyService.hasFreeTravel(player.location, warpLocation)) return 0.0
        }

        if (configService.getTeleportCostType() != CostType.ITEM) return baseCost
        if (!configService.isTeleportCostDistanceScaling()) return baseCost

        val min = configService.getTeleportCostMin()
        val max = configService.getTeleportCostMax()
        val scaleDistance = configService.getTeleportCostScaleDistance()

        // Cross-world teleport always costs max
        if (player.world.uid != warp.worldId) return max.toDouble()

        val dx = player.location.x - warp.position.x
        val dz = player.location.z - warp.position.z
        val distance = Math.sqrt(dx * dx + dz * dz)

        val ratio = Math.sqrt(distance / scaleDistance).coerceIn(0.0, 1.0)
        return (min + (max - min) * ratio).toInt().coerceIn(min, max).toDouble()
    }

    private fun hasCost(player: Player, warp: Warp): Boolean {
        if (!configService.isTeleportCostEnabled()) return true
        if (player.hasPermission("waystonewarps.bypass.cost")) return true
        val teleportCost = calculateCostInternal(player, warp)

        return when (configService.getTeleportCostType()) {
            CostType.ITEM -> {
                val material = try {
                    Material.valueOf(configService.getTeleportCostItem())
                } catch (_: IllegalArgumentException) {
                    Material.ENDER_PEARL
                }
                val itemModel = configService.getTeleportCostItemModel().takeIf { it.isNotBlank() }
                hasEnoughItems(player, material, teleportCost, itemModel)
            }
            CostType.MONEY -> hasEnoughMoney(player, teleportCost)
            CostType.XP -> hasEnoughXp(player, teleportCost)
        }
    }

    private fun deductCost(player: Player, warp: Warp) {
        if (!configService.isTeleportCostEnabled()) return
        if (player.hasPermission("waystonewarps.bypass.cost")) return
        val teleportCost = calculateCostInternal(player, warp)
        when (configService.getTeleportCostType()) {
            CostType.ITEM -> {
                val material = try {
                    Material.valueOf(configService.getTeleportCostItem())
                } catch (_: IllegalArgumentException) {
                    Material.ENDER_PEARL
                }
                val itemModel = configService.getTeleportCostItemModel().takeIf { it.isNotBlank() }
                removeItems(player, material, teleportCost, itemModel)
            }
            CostType.MONEY -> subtractMoney(player, teleportCost)
            CostType.XP -> subtractXp(player, teleportCost)
        }
    }

    private fun itemMatches(item: org.bukkit.inventory.ItemStack, itemMaterial: Material, itemModel: String?): Boolean {
        if (item.type != itemMaterial) return false
        if (itemModel == null) return true
        val model = item.getData(DataComponentTypes.ITEM_MODEL) ?: return false
        return model == Key.key(itemModel)
    }

    private fun hasEnoughItems(player: Player, itemMaterial: Material, teleportCost: Double, itemModel: String?): Boolean {
        var count = teleportCost.toInt()
        if (count <= 0) return true
        for (item in player.inventory.contents.filterNotNull()) {
            if (itemMatches(item, itemMaterial, itemModel)) {
                val removeAmount = minOf(item.amount, count)
                count -= removeAmount
                if (count <= 0) {
                    return true
                }
            }
        }
        return false
    }

    private fun removeItems(player: Player, itemMaterial: Material, teleportCost: Double, itemModel: String?) {
        var count = teleportCost.toInt()
        for (item in player.inventory.contents.filterNotNull()) {
            if (itemMatches(item, itemMaterial, itemModel)) {
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
        economy?.withdrawPlayer(player, teleportCost)
    }

    private fun hasEnoughXp(player: Player, teleportCost: Double): Boolean {
        return player.level >= teleportCost
    }

    private fun subtractXp(player: Player, teleportCost: Double) {
        player.giveExpLevels(-teleportCost.toInt())
    }

    private fun getLandingOffset(playerYaw: Float): Pair<Int, Int> {
        return if (playerYaw >= -22.5 && playerYaw < 22.5) Pair(0, 1)       // SOUTH
        else if (playerYaw >= 22.5 && playerYaw < 67.5) Pair(-1, 1)         // SOUTH_EAST
        else if (playerYaw >= 67.5 && playerYaw < 112.5) Pair(-1, 0)        // EAST
        else if (playerYaw >= 112.5 && playerYaw < 157.5) Pair(-1, -1)      // NORTH_EAST
        else if (playerYaw >= 157.5 || playerYaw < -157.5) Pair(0, -1)      // NORTH
        else if (playerYaw >= -157.5 && playerYaw < -112.5) Pair(1, -1)     // NORTH_WEST
        else if (playerYaw >= -112.5 && playerYaw < -67.5) Pair(1, 0)       // WEST
        else Pair(1, 1)                                                       // SOUTH_WEST
    }

    private fun getOffsetLocation(location: Location, playerYaw: Float): Location {
        val (dx, dz) = getLandingOffset(playerYaw)
        return location.add(dx.toDouble(), 0.0, dz.toDouble())
    }


    private data class PendingTeleport(
        val taskHandle: Task,
        val onCanceled: () -> Unit
    )

    private fun getPlayerState(playerId: UUID): PlayerState {
        val existing = playerStateRepository.getById(playerId)
        if (existing != null) return existing

        val state = PlayerState(playerId)
        playerStateRepository.add(state)
        return state
    }

    private fun clearArea(location: Location, playerYaw: Float) {
        val (dx, dz) = getLandingOffset(playerYaw)
        // Clear only the two blocks the player will occupy at the landing spot (feet and head)
        for (dy in -1..0) {
            val block = location.world.getBlockAt(location.blockX + dx, location.blockY + dy, location.blockZ + dz)
            if (!block.type.isAir) {
                block.breakNaturally()
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
