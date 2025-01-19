package dev.mizarc.waystonewarps.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import dev.mizarc.waystonewarps.Config
import dev.mizarc.waystonewarps.Teleporter
import dev.mizarc.waystonewarps.utils.LocationConversions

@CommandAlias("spawn")
@CommandPermission("worldwidewarps.command.spawn")
class SpawnCommand: BaseCommand() {
    @Dependency lateinit var config: Config
    @Dependency lateinit var teleporter: Teleporter

    @Default
    fun onSpawn(player: Player) {
        // Cancel if spawn doesn't exist
        val spawnLocationString = config.spawnLocation
        if (spawnLocationString.isBlank()) {
            player.sendActionBar(
                Component
                .text("There is no set spawn on this server.")
                    .color(TextColor.color(255, 85, 85)))
            return
        }

        // Cancel if set spawn location is invalid
        val spawnLocation = LocationConversions.stringTolocation(spawnLocationString)
        if (spawnLocation == null) {
            player.sendPlainMessage("§cThe spawn is invalid. Ask an administrator to fix this.")
            return
        }

        // Teleports the player
        teleporter.teleportSpawn(player)
    }
}