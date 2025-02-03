package dev.mizarc.waystonewarps.infrastructure.mappers

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

fun Location.toPosition3D(): Position3D {
    return Position3D(this.blockX, this.blockY, this.blockZ)
}

fun Position3D.toLocation(world: World): Location {
    return Location(world, this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}

fun locationToString(location: Location): String {
    return "${location.world!!.name} ${location.x} ${location.y} ${location.z} ${location.yaw} ${location.pitch}"
}

fun stringToLocation(string: String): Location? {
    val splitString = string.split(" ")
    val world = Bukkit.getServer().getWorld(splitString[0])
    return try {
        val x = splitString[1].toDouble()
        val y = splitString[2].toDouble()
        val z = splitString[3].toDouble()
        val yaw = splitString[4].toFloat()
        val pitch = splitString[5].toFloat()
        Location(world, x, y, z, yaw, pitch)
    } catch(except: NumberFormatException) {
        null
    }
}