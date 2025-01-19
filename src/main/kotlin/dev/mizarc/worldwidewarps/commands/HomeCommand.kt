package dev.mizarc.worldwidewarps.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import org.bukkit.entity.Player
import dev.mizarc.worldwidewarps.Config
import dev.mizarc.worldwidewarps.domain.PlayerRepository
import dev.mizarc.worldwidewarps.Teleporter

@CommandAlias("home")
@CommandPermission("worldwidewarps.command.home")
class HomeCommand: BaseCommand() {
    @Dependency lateinit var config: Config
    @Dependency lateinit var teleporter: Teleporter
    @Dependency lateinit var playerRepository: PlayerRepository

    @Default
    fun onHome(player: Player) {
        // Get the player's bed location
        val bedSpawnLocation = player.bedSpawnLocation
        if (bedSpawnLocation == null) {
            player.sendPlainMessage("§cYou don't have a home. Sleep in a bed to set your home.")
            return
        }

        teleporter.teleportHome(player)
    }
}