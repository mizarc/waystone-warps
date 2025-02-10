package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import java.util.UUID

class StructureBuilderServiceBukkit: StructureBuilderService {

    override fun spawnStructure(warp: Warp) {
        // Replace bottom block with barrier
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        val bottomBlock = world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ)
        bottomBlock.type = Material.BARRIER

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

    override fun despawnStructure(warp: Warp) {
        // Change lower block back into smooth stone
        val world = Bukkit.getWorld(warp.worldId) ?: return
        val location = warp.position.toLocation(world)
        val bottomBlock = world.getBlockAt(location.blockX, location.blockY - 1, location.blockZ)
        bottomBlock.type = Material.SMOOTH_STONE

        // Remove connected block display entities
        val entities: MutableList<Entity> = location.world.entities
        for (entity in entities) {
            val customName = entity.customName() ?: continue
            if (customName is TextComponent && customName.content() == warp.id.toString()) {
                entity.remove()
            }
        }
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
}