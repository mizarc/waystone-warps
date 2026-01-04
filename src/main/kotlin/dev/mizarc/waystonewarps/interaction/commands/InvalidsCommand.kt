package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import dev.mizarc.waystonewarps.application.actions.administration.ListInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveAllInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveInvalidWarpsForWorld
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@CommandAlias("waystonewarps|ww")
class InvalidsCommand : BaseCommand(), KoinComponent {
    private val listInvalidWarps: ListInvalidWarps by inject()
    private val removeAllInvalidWarps: RemoveAllInvalidWarps by inject()
    private val removeInvalidWarpsForWorld: RemoveInvalidWarpsForWorld by inject()
    private val localizationProvider: LocalizationProvider by inject()

    @Subcommand("invalids list")
    @CommandPermission("waystonewarps.admin.invalids.list")
    @Description("List all warps in invalid worlds")
    fun onListInvalids(sender: CommandSender) {
        val warps = listInvalidWarps.listAllInvalidWarps()

        if (warps.isEmpty()) {
            val message = if (sender is Player) {
                localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_NO_INVALID_WARPS)
            } else {
                localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_NO_INVALID_WARPS)
            }
            sender.sendMessage(Component.text(message, NamedTextColor.GREEN))
            return
        }

        // Group warps by world ID and count them
        val warpsByWorld = warps.groupBy { it.worldId }
            .mapValues { (_, warpsInWorld) -> warpsInWorld.size }
            .toList()
            .sortedByDescending { (_, count) -> count }

        val headerMessage = if (sender is Player) {
            localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_LIST_HEADER, warps.size)
        } else {
            localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_LIST_HEADER, warps.size)
        }
        val header = Component.text(headerMessage, NamedTextColor.GOLD)
        sender.sendMessage(header)
        warpsByWorld.forEach { (worldId, count) ->
            val worldIdStr = worldId.toString()
            val worldEntryMessage = if (sender is Player) {
                localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_LIST_WORLD_ENTRY, worldIdStr, count)
            } else {
                localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_LIST_WORLD_ENTRY, worldIdStr, count)
            }
            val message = Component.text()
                .content(worldEntryMessage)
                .color(NamedTextColor.GRAY)
            
            if (sender is Player) {
                // For players, add click and hover events
                val hoverText = if (sender is Player) {
                    localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_LIST_CLIPBOARD_HOVER)
                } else {
                    localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_LIST_CLIPBOARD_HOVER)
                }
                val clickableMessage = message
                    .clickEvent(ClickEvent.copyToClipboard(worldIdStr))
                    .hoverEvent(HoverEvent.showText(Component.text(hoverText, NamedTextColor.GREEN)))
                
                sender.sendMessage(clickableMessage)
            } else {
                // Fallback for console/non-player senders
                sender.sendMessage(message)
            }
        }
    }

    @Subcommand("invalids remove")
    @CommandPermission("waystonewarps.admin.invalids.remove")
    @Description("Remove warps in a specific world")
    fun onRemoveInvalids(sender: CommandSender, @Name("world") worldId: String) {
        try {
            val uuid = try {
                UUID.fromString(worldId)
            } catch (e: IllegalArgumentException) {
                // Try to find world by name if UUID parsing fails
                val world = Bukkit.getWorld(worldId)
                if (world == null) {
                    val errorMessage = if (sender is Player) {
                        localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_REMOVE_INVALID_WORLD, worldId)
                    } else {
                        localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_REMOVE_INVALID_WORLD, worldId)
                    }
                    sender.sendMessage(Component.text(errorMessage, NamedTextColor.RED))
                    return
                }
                world.uid
            }
            
            val (removed, total) = removeInvalidWarpsForWorld.execute(uuid)
            val world = Bukkit.getWorld(uuid)
            val worldName = world?.name ?: "Unknown World ($uuid)"
            
            val successMessage = if (sender is Player) {
                localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_REMOVE_SUCCESS, removed, worldName)
            } else {
                localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_REMOVE_SUCCESS, removed, worldName)
            }
            
            val message = Component.text(successMessage, NamedTextColor.GREEN)
                .colorIfAbsent(NamedTextColor.GREEN)
                
            sender.sendMessage(message)
        } catch (e: Exception) {
            val errorMessage = if (sender is Player) {
                localizationProvider.get(sender.uniqueId, LocalizationKeys.COMMAND_INVALIDS_REMOVE_ERROR, e.message ?: "Unknown error")
            } else {
                localizationProvider.getConsole(LocalizationKeys.COMMAND_INVALIDS_REMOVE_ERROR, e.message ?: "Unknown error")
            }
            sender.sendMessage(Component.text(errorMessage, NamedTextColor.RED))
        }
    }

    @Subcommand("invalids removeall")
    @CommandPermission("waystonewarps.admin.invalids.removeall")
    @Description("Remove all warps in invalid worlds")
    fun onRemoveAllInvalids(sender: CommandSender) {
        val (removed, total) = removeAllInvalidWarps.execute()
        val message = if (sender is Player) {
            localizationProvider.get(sender.uniqueId, "command.invalids.remove_all.success", removed)
        } else {
            localizationProvider.getConsole("command.invalids.remove_all.success", removed)
        }
        sender.sendMessage(Component.text(message, NamedTextColor.GREEN))
    }
}