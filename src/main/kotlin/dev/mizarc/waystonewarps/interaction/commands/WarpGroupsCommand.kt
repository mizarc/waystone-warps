package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.admin.WarpGroupManagementMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("warpgroups")
@CommandPermission("waystonewarps.admin.manage_groups")
class WarpGroupsCommand : BaseCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    @Default
    fun onWarpGroups(player: Player) {
        val menuNavigator = MenuNavigator(player)
        WarpGroupManagementMenu(player, menuNavigator, localizationProvider).open()
    }
}
