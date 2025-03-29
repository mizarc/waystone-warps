package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import org.bukkit.entity.Player

@CommandAlias("warpoverride")
@CommandPermission("worldwidewarps.command.warpoverride")
class WarpOverrideCommand: BaseCommand() {

    @Default
    fun onWarpOverride(player: Player) {
    }
}