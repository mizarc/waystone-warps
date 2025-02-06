package dev.mizarc.waystonewarps.interaction.models

import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.time.Instant
import java.util.*

class WarpInfo(val id: UUID, val player: OfflinePlayer, val creationTime: Instant,
               var name: String, var location: Location?, var icon: Material)

fun Warp.toViewModel(): WarpInfo {
    val player = Bukkit.getOfflinePlayer(playerId)
    val material = Material.getMaterial(this.icon.uppercase()) ?: Material.LODESTONE
    val world = Bukkit.getWorld(worldId)
    val location: Location? = if (world != null) {
        this.position.toLocation(world)
    } else {
        null
    }

    return WarpInfo(
        id = id,
        player = player,
        creationTime = creationTime,
        name = name,
        location = location,
        icon = material
    )
}