package dev.mizarc.worldwidewarps.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player
import dev.mizarc.worldwidewarps.Config
import dev.mizarc.worldwidewarps.domain.PlayerRepository
import dev.mizarc.worldwidewarps.Teleporter

@CommandAlias("setspawn")
@CommandPermission("worldwidewarps.command.setspawn")
class SetspawnCommand: BaseCommand() {
    @Dependency lateinit var config: Config
    @Dependency lateinit var teleporter: Teleporter
    @Dependency lateinit var playerRepository: PlayerRepository

    @Default
    fun onSetspawn(player: Player) {
        // Teleport the player
        config.setSpawnLocation(player.location)
        player.sendMessage("§aSpawn has been set to your location.")
    }
}