package dev.mizarc.worldwidewarps.listeners

import dev.geco.gsit.api.GSitAPI
import dev.geco.gsit.api.event.PrePlayerGetUpPoseEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.Bed
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.util.Vector
import dev.mizarc.worldwidewarps.*
import dev.mizarc.worldwidewarps.domain.Home
import dev.mizarc.worldwidewarps.domain.HomeRepository
import dev.mizarc.worldwidewarps.domain.PlayerRepository
import dev.mizarc.worldwidewarps.menus.BedMenu

class BedInteractListener(private val homes: HomeRepository, private val players: PlayerRepository): Listener {
    @EventHandler
    fun onBedShiftClick(event: PlayerBedEnterEvent) {
        // Prevent player from sleeping on someone else's bed
        val existingHome = homes.getAll().find { it.position == Position(event.bed.location) }
        if (existingHome != null && existingHome.player.uniqueId != event.player.uniqueId) {
            event.player.sendActionBar(Component.text("This bed is owned by ${existingHome.player.name}")
                .color(TextColor.color(255, 85, 85)))
            event.isCancelled = true
            return
        }

        // Permission check for bed menu
        if (!event.player.hasPermission("worldwidewarps.action.multi_home")
            || !event.player.isSneaking || event.bed.blockData !is Bed) {
            return
        }
        event.isCancelled = true

        // Prevent sleeping on obstructed bed
        if (isBedObstructed(event.bed)) {
            event.player.sendActionBar(Component.text("This bed is obstructed")
                .color(TextColor.color(255, 85, 85)))
            return
        }

        // Set player's view to align with the bed
        val bed = event.bed.blockData as Bed
        val direction = Direction.fromVector(bed.facing.oppositeFace.direction)
        val newLocation = event.bed.location
        newLocation.pitch = 0.0f
        newLocation.yaw = Direction.toYaw(direction)
        event.player.teleport(newLocation)

        // Set player's pose to look like they're sleeping
        GSitAPI.createPose(event.bed, event.player, Pose.SLEEPING, 0.0,
            0.0, 0.0, Direction.toYaw(direction), true)
        event.player.bedSpawnLocation = event.bed.location

        // Create menu
        val homeBuilder = Home.Builder(event.player, event.bed.world, Position(event.bed.location), bed)
        val playerState = players.getByPlayer(event.player) ?: return
        BedMenu(homes, playerState, homeBuilder).openHomeSelectionMenu()
    }

    @EventHandler
    fun onBedMenuCloseEvent(event: InventoryCloseEvent) {
        val playerState = players.getByPlayer(event.player as Player) ?: return
        if (playerState.inBedMenu) {
            playerState.inBedMenu = false
            playerState.isLaying = true
        }
    }

    @EventHandler
    fun onPlayerGetUpPoseEvent(event: PrePlayerGetUpPoseEvent) {
        val playerState = players.getByPlayer(event.player) ?: return
        if (playerState.inBedMenu) {
            event.isCancelled = true
        }

        if (playerState.isLaying) {
            playerState.isLaying = false
            //event.player.bedSpawnLocation?.let { bedSpawnLocation ->
            //    event.player.teleport(bedSpawnLocation.world.getS bedSpawnLocation)
            //}
        }
    }

    fun isBedObstructed(bedBlock: Block): Boolean {
        val bed = bedBlock.blockData as Bed
        val checkLocations = arrayOf(bedBlock.location.add(Vector(0, 1, 0)),
            bedBlock.getRelative(bed.facing.oppositeFace).location.add(Vector(0, 1, 0)))

        for (location in checkLocations) {
            val block = location.block
            if (block.type != Material.AIR && block.type != Material.CAVE_AIR) {
                return true
            }
        }

        return false
    }
}