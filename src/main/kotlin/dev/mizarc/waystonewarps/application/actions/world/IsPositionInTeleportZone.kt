package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class IsPositionInTeleportZone(private val warpRepository: WarpRepository) {
    fun execute(position: Position3D, worldId: UUID): Boolean {
        val warps = warpRepository.getAll()
        val worldWarps = warps.filter { it -> it.worldId ==  worldId}

        for (warp in worldWarps) {
            if (isInTeleportZone(warp.position, position)) {
                return true
            }
        }
        return false
    }

    fun isInTeleportZone(center: Position3D, target: Position3D): Boolean {
        // Calculate the boundaries of the teleport zone
        val minX = center.x - 1
        val maxX = center.x + 1
        val minY = center.y - 1
        val maxY = center.y
        val minZ = center.z - 1
        val maxZ = center.z + 1

        // Check if the target location is within the boundaries
        return target.x in minX..maxX &&
                target.y in minY..maxY &&
                target.z in minZ..maxZ
    }
}