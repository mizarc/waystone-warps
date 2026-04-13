package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Name
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("warpstone")
@CommandPermission("waystonewarps.admin.givewarpstone")
@Description("Give a player warpstone items")
class GiveWarpstoneCommand : BaseCommand(), KoinComponent {

    private val localizationProvider: LocalizationProvider by inject()

    @Default
    @CommandCompletion("@players 1-64")
    fun onGiveWarpstone(sender: CommandSender, @Name("player") playerName: String, @Name("amount") @Default("1") amount: Int) {
        val target = Bukkit.getPlayer(playerName)
        if (target == null) {
            sender.sendMessage(Component.text("Player '$playerName' not found or not online.").color(NamedTextColor.RED))
            return
        }

        val item = ItemStack(Material.ECHO_SHARD, amount.coerceAtLeast(1))
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key("minecraft:warpstone"))
        item.setData(DataComponentTypes.CUSTOM_NAME,
            Component.text("Warp Stone")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false))
        item.setData(DataComponentTypes.LORE, ItemLore.lore()
            .addLine(Component.text("A compact stone attuned to long-distance travel.")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, true))
            .build())

        target.inventory.addItem(item)

        val senderMessage = localizationProvider.getConsole(
            LocalizationKeys.COMMAND_GIVE_WARPSTONE_SUCCESS_SENDER,
            amount, target.name)
        val targetMessage = localizationProvider.get(
            target.uniqueId,
            LocalizationKeys.COMMAND_GIVE_WARPSTONE_SUCCESS_TARGET,
            amount)

        sender.sendMessage(Component.text(senderMessage).color(NamedTextColor.GREEN))
        target.sendMessage(Component.text(targetMessage).color(NamedTextColor.GREEN))
    }
}
