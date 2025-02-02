package dev.mizarc.waystonewarps.infrastructure.mappers

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import org.bukkit.Location
import org.bukkit.World

fun Location.toPosition3D(): Position3D {
    return Position3D(this.blockX, this.blockY, this.blockZ)
}

fun Position3D.toLocation(world: World): Location {
    return Location(world, this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}