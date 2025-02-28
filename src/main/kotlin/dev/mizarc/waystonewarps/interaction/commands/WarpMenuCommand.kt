package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import org.bukkit.entity.Player

@CommandAlias("warpmenu")
@CommandPermission("worldwidewarps.command.warpmenu")
class WarpMenuCommand: BaseCommand() {

    @Default
    fun onWarp(player: Player, @Optional backCommand: String? = null) {
        val menuNavigator = MenuNavigator(player)
        WarpMenu(player, menuNavigator).open()
    }
}