package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import org.bukkit.entity.Player
import dev.mizarc.waystonewarps.Teleporter
import dev.mizarc.waystonewarps.interaction.menus.WarpMenu

@CommandAlias("warpmenu")
@CommandPermission("worldwidewarps.command.warpmenu")
class WarpMenuCommand: BaseCommand() {
    @Dependency lateinit var teleporter: Teleporter
    @Dependency lateinit var warpAccessRepo: WarpAccessRepository

    @Default
    fun onWarp(player: Player, backCommand: String? = null) {
        WarpMenu(warpAccessRepo, teleporter, player).openWarpMenu(backCommand)
    }
}