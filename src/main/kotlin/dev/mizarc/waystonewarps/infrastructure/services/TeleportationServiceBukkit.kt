package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.*
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TeleportationServiceBukkit(private val playerAttributeService: PlayerAttributeService,
                                 private val movementMonitorService: MovementMonitorService,
                                 private val scheduler: Scheduler
): TeleportationService {
    private val activeTeleportations = ConcurrentHashMap<UUID, PendingTeleport>()

    override fun teleportPlayer(playerId: UUID, warp: Warp): Result<Unit> {
        // Location data
        val world = Bukkit.getWorld(warp.worldId) ?: return Result.failure(Exception("World not found."))
        val warpLocation = warp.position.toLocation(world)
        warpLocation.x += 0.5
        warpLocation.y += 1
        warpLocation.z += 0.5

        // Player data
        val cost = playerAttributeService.getTeleportCost(playerId)
        val player = Bukkit.getPlayer(playerId) ?: return Result.failure(Exception("Player not found."))

        // Generate offset location based on player pitch and yaw
        val offsetLocation = getOffsetLocation(warpLocation, player.yaw)
        offsetLocation.pitch = player.pitch
        offsetLocation.yaw = player.yaw
        offsetLocation.add(0.0, -2.0, 0.0)

        // Teleports the player instantaneously
        removeCostFromInventory(player, cost)
        player.teleport(offsetLocation)
        return Result.success(Unit)
    }

    override fun scheduleDelayedTeleport(playerId: UUID, warp: Warp, delaySeconds: Int,
                                         onSuccess: () -> Unit, onCanceled: () -> Unit): Result<Unit> {
        // Cancel existing pending teleport if any
        activeTeleportations[playerId]?.let {
            it.taskHandle.cancel()
            movementMonitorService.stopMonitoringPlayer(playerId)
            it.onCanceled()
        }

        // Schedule the new teleportation task
        val taskHandle = scheduler.schedule(delaySeconds * 20L) {
            movementMonitorService.stopMonitoringPlayer(playerId)
            activeTeleportations.remove(playerId)
            val teleportResult = teleportPlayer(playerId, warp)
            if (teleportResult.isSuccess) {
                onSuccess()
            } else {
                // Handle teleportation failure if needed
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

        return Result.success(Unit)
    }

    override fun cancelPendingTeleport(playerId: UUID): Result<Unit> {
        val pendingTeleport = activeTeleportations.remove(playerId)
            ?: return Result.failure(Exception("No pending teleport to cancel."))
        movementMonitorService.stopMonitoringPlayer(playerId)
        pendingTeleport.taskHandle.cancel()
        pendingTeleport.onCanceled()
        return Result.success(Unit)
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

    private fun getOffsetLocation(location: Location, playerYaw: Float): Location {
        return if (playerYaw >= -22.5 && playerYaw < 22.5) location.add(0.0, 0.0, 1.0); // SOUTH
        else if (playerYaw >= 22.5 && playerYaw < 67.5) location.add(-1.0, 0.0, 1.0); // SOUTH_EAST
        else if (playerYaw >= 67.5 && playerYaw < 112.5) location.add(-1.0, 0.0, 0.0); // EAST
        else if (playerYaw >= 112.5 && playerYaw < 157.5) location.add(-1.0, 0.0, -1.0); // NORTH_EAST
        else if (playerYaw >= 157.5 || playerYaw < -157.5) location.add(0.0, 0.0, -1.0); // NORTH
        else if (playerYaw >= -157.5 && playerYaw < -112.5) location.add(1.0, 0.0, -1.0); // NORTH_WEST
        else if (playerYaw >= -112.5 && playerYaw < -67.5) location.add(1.0, 0.0, 0.0); // WEST
        else location.add(1.0, 0.0, 1.0);
    }


    private data class PendingTeleport(
        val taskHandle: TaskHandle,
        val onCanceled: () -> Unit
    )
}