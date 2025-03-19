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
        // Get the structure blocks based on the block type
        val structureBlocks = configService.getStructureBlocks(warp.block).takeIf { it.count() == 5 }
            ?: listOf("SMOOTH_STONE", "LODESTONE", "SMOOTH_STONE", "SMOOTH_STONE", "SMOOTH_STONE_SLAB")
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)

        // Replace top block with main block type
        location.block.type = Material.valueOf(structureBlocks[1])

        // Replace bottom block with slab
        // Needs to be a 2 tick delay here because Bukkit is ass and spits out a stupid POI data mismatch error
        object : BukkitRunnable() {
            override fun run() {
                world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ)
                    .type = Material.valueOf(structureBlocks[4])
            }
        }.runTaskLater(plugin, 2L)

        // Generate custom model
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.valueOf(structureBlocks[0]),
            0.075f, 1.3f, 0.075f,
            0.85f, 0.85f, 0.85f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.valueOf(structureBlocks[2]),
            0.075f, 0.8f, 0.075f,
            0.85f, 0.85f, 0.85f)
        createBlockDisplay(warp.id, warp.position.toLocation(world), Material.valueOf(structureBlocks[3]),
            0.2f, 0.4f, 0.2f,
            0.6f, 0.6f, 0.6f)
    }

    override fun updateStructure(warp: Warp) {
        val structureBlocks = configService.getStructureBlocks(warp.block).takeIf { it.count() == 5 }
            ?: listOf("SMOOTH_STONE", "LODESTONE", "SMOOTH_STONE", "SMOOTH_STONE", "SMOOTH_STONE_SLAB")
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)

        // Replace top block with main block type
        location.block.type = Material.valueOf(structureBlocks[1])

        // Replace bottom block with slab
        world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ)
            .type = Material.valueOf(structureBlocks[4])

        // Generate custom model
        val entityList = mutableListOf<Entity>()
        entityList.add(createBlockDisplay(warp.id, warp.position.toLocation(world), Material.valueOf(structureBlocks[0]),
            0.075f, 1.3f, 0.075f,
            0.85f, 0.85f, 0.85f))
        entityList.add(createBlockDisplay(warp.id, warp.position.toLocation(world), Material.valueOf(structureBlocks[2]),
            0.075f, 0.8f, 0.075f,
            0.85f, 0.85f, 0.85f))
        entityList.add(createBlockDisplay(warp.id, warp.position.toLocation(world), Material.valueOf(structureBlocks[3]),
            0.2f, 0.4f, 0.2f,
            0.6f, 0.6f, 0.6f))

        // Remove existing block display after 2 ticks to ensure it doesn't disappear before new one is spawned
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

    private fun createBlockDisplay(warpId: UUID, baseLocation: Location, material: Material,
                                   offsetX: Float, offsetY: Float, offsetZ: Float,
                                   scaleX: Float, scaleY: Float, scaleZ: Float): Entity {
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