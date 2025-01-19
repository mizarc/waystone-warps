package dev.mizarc.waystonewarps

import net.milkbowl.vault.chat.Chat
import org.bukkit.entity.Player

class PlayerState(val player: Player, private val config: Config, private val metadata: Chat) {
    var teleportTask: TeleportTask? = null
    var isLaying: Boolean = false
    var inBedMenu: Boolean = false

    fun getHomeLimit(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.home_limit", config.homeLimit)
            .takeIf { it > -1 } ?: -1

    fun getHomeTeleportCost(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.home_teleport_cost", config.homeCost)
            .takeIf { it > -1 } ?: -1

    fun getHomeTeleportTimer(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.home_teleport_timer", config.homeTimer)
            .takeIf { it > -1 } ?: -1

    fun getSpawnTeleportCost(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.spawn_teleport_cost", config.spawnCost)
            .takeIf { it > -1 } ?: -1

    fun getSpawnTeleportTimer(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.spawn_teleport_timer", config.spawnTimer)
            .takeIf { it > -1 } ?: -1

    fun getWarpTeleportCost(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.warp_teleport_cost", config.warpCost)
            .takeIf { it > -1 } ?: -1

    fun getWarpTeleportTimer(): Int =
        metadata.getPlayerInfoInteger(player, "worldwidewarps.warp_teleport_timer", config.warpTimer)
            .takeIf { it > -1 } ?: -1
}