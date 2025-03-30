package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
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

class StructureBuilderServiceBukkit(private val plugin: Plugin, private val configService: ConfigService): StructureBuilderService {

    override fun spawnStructure(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val structureBlocks = getStructureBlocks(warp)
        generateStructure(warp, structureBlocks, world)
    }

    override fun updateStructure(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val structureBlocks = getStructureBlocks(warp)

        // Generate and then remove existing block display after 2 ticks to prevent flashing
        val entityList = generateStructure(warp, structureBlocks, world)
        object : BukkitRunnable() {
            override fun run() {
                removeBlockDisplay(warp, world, entityList)
            }
        }.runTaskLater(plugin, 2L)
    }

    override fun revertStructure(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ).type = Material.valueOf(warp.block)
        removeBlockDisplay(warp, world)
    }

    override fun destroyStructure(warp: Warp) {
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        location.block.type = Material.AIR
        world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ).type = Material.AIR
        removeBlockDisplay(warp, world)
    }

    private fun getStructureBlocks(warp: Warp): List<Material> {
        val defaultStructureBlocks = listOf(Material.SMOOTH_STONE, Material.LODESTONE, Material.SMOOTH_STONE,
            Material.SMOOTH_STONE, Material.SMOOTH_STONE_SLAB
        )
        return try {
            val blocks = configService.getStructureBlocks(warp.block)
            if (blocks.count() == 5) {
                blocks.map { Material.valueOf(it) }
            } else {
                defaultStructureBlocks
            }
        } catch (_: IllegalArgumentException) {
            defaultStructureBlocks
        }
    }

    private fun generateStructure(warp: Warp, structureBlocks: List<Material>, world: World): MutableList<Entity> {
        val location = warp.position.toLocation(world)

        // Replace top block with main block type
        location.block.type = structureBlocks[1]

        // Replace bottom block with slab (delay to avoid POI data mismatch error)
        object : BukkitRunnable() {
            override fun run() {
                world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ).type = structureBlocks[4]
            }
        }.runTaskLater(plugin, 2L)

        // Create and return entities
        return mutableListOf(
            createBlockDisplay(warp.id, location, structureBlocks[0],
                0.075f, 1.3f, 0.075f,
                0.85f, 0.85f, 0.85f),
            createBlockDisplay(warp.id, location, structureBlocks[2],
                0.075f, 0.8f, 0.075f,
                0.85f, 0.85f, 0.85f),
            createBlockDisplay(warp.id, location, structureBlocks[3],
                0.2f, 0.4f, 0.2f,
                0.6f, 0.6f, 0.6f)
        )
    }

    private fun createBlockDisplay(warpId: UUID, baseLocation: Location, material: Material,
                                   offsetX: Float, offsetY: Float, offsetZ: Float,
                                   scaleX: Float, scaleY: Float, scaleZ: Float): Entity {
        // Create BlockData
        val blockData = material.createBlockData()
        val location = baseLocation.clone()
        location.y -= 1
        val blockDisplay = baseLocation.world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay
        blockDisplay.block = blockData

        // Transform display
        val transformation = Transformation(
            Vector3f(offsetX, offsetY, offsetZ), AxisAngle4f(),
            Vector3f(scaleX, scaleY, scaleZ), AxisAngle4f())
        blockDisplay.transformation = transformation
        blockDisplay.customName(Component.text((warpId.toString())))

        return blockDisplay
    }

    private fun removeBlockDisplay(warp: Warp, world: World, entityExclusions: List<Entity> = listOf()) {
        val entities: MutableList<Entity> = world.entities
        for (entity in entities) {
            if (entityExclusions.contains(entity)) continue
            val customName = entity.customName() ?: continue
            if (customName is TextComponent && customName.content() == warp.id.toString()) {
                entity.remove()
            }
        }
    }
}