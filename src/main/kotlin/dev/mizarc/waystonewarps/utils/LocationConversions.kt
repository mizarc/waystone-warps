package dev.mizarc.waystonewarps.utils

import org.bukkit.Bukkit
import org.bukkit.Location

class LocationConversions {
    companion object {
        fun locationToString(location: Location): String {
            return "${location.world!!.name} ${location.x} ${location.y} ${location.z} ${location.yaw} ${location.pitch}"
        }

        fun stringTolocation(string: String): Location? {
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
    }
}