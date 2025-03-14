package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.*

class StructureBuilderServiceBukkit(private val plugin: Plugin): StructureBuilderService {

    override fun spawnStructure(warp: Warp) {
        // Replace bottom block with barrier
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        location.block.type = Material.LODESTONE

        // Needs to be a 2 tick delay here because Bukkit is ass and spits out a stupid POI data mismatch error
        object : BukkitRunnable() {
            override fun run() {
                world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ).type = Material.BARRIER
            }
        }.runTaskLater(plugin, 2L)

        // Generate custom model
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE_SLAB,
            0.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE,
            0.075f, 0.8f, 0.075f,
            0.85f, 0.85f, 0.85f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE,
            0.2f, 0.4f, 0.2f,
            0.6f, 0.6f, 0.6f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.SMOOTH_STONE,
            0.075f, 1.3f, 0.075f,
            0.85f, 0.85f, 0.85f)
    }

    override fun revertStructure(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ).type = Material.SMOOTH_STONE
        removeBlockDisplay(warp, world)
    }

    override fun destroyStructure(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        location.block.type = Material.AIR
        world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ).type = Material.AIR
        removeBlockDisplay(warp, world)
    }

    private fun createBlockDisplay(warpId: UUID, baseLocation: Location, material: Material,
                                   offsetX: Float, offsetY: Float, offsetZ: Float,
                                   scaleX: Float, scaleY: Float, scaleZ: Float) {
        // Create BlockData
        val blockData = material.createBlockData()
        baseLocation.y -= 1
        val blockDisplay = baseLocation.world.spawnEntity(baseLocation, EntityType.BLOCK_DISPLAY) as BlockDisplay
        blockDisplay.block = blockData

        // Transform display
        val transformation = Transformation(
            Vector3f(offsetX, offsetY, offsetZ), AxisAngle4f(),
            Vector3f(scaleX, scaleY, scaleZ), AxisAngle4f())
        blockDisplay.transformation = transformation
        blockDisplay.customName(Component.text((warpId.toString())))
    }

    private fun removeBlockDisplay(warp: Warp, world: World) {
        val entities: MutableList<Entity> = world.entities
        for (entity in entities) {
            val customName = entity.customName() ?: continue
            if (customName is TextComponent && customName.content() == warp.id.toString()) {
                entity.remove()
            }
        }
    }
}