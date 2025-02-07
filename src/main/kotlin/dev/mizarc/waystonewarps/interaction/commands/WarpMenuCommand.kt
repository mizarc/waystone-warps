package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player

@CommandAlias("warpmenu")
@CommandPermission("worldwidewarps.command.warpmenu")
class WarpMenuCommand: BaseCommand() {

    @Default
    fun onWarp(player: Player, backCommand: String? = null) {
        // TODO: Implement command
    }
}