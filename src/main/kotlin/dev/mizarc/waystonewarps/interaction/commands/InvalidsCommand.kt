package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import dev.mizarc.waystonewarps.application.actions.administration.ListInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveAllInvalidWarps
import dev.mizarc.waystonewarps.application.actions.administration.RemoveInvalidWarpsForWorld
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
@CommandPermission("waystonewarps.admin.invalids")
class InvalidsCommand : BaseCommand(), KoinComponent {
    private val listInvalidWarps: ListInvalidWarps by inject()
    private val removeAllInvalidWarps: RemoveAllInvalidWarps by inject()
    private val removeInvalidWarpsForWorld: RemoveInvalidWarpsForWorld by inject()

    @Subcommand("invalids list")
    @CommandPermission("waystonewarps.admin.invalids.list")
    @Description("List all warps in invalid worlds")
    fun onListInvalids(sender: CommandSender) {
        val warps = listInvalidWarps.listAllInvalidWarps()

        if (warps.isEmpty()) {
            sender.sendMessage(Component.text("No invalid warps found.", NamedTextColor.GREEN))
            return
        }

        // Group warps by world ID and count them
        val warpsByWorld = warps.groupBy { it.worldId }
            .mapValues { (_, warpsInWorld) -> warpsInWorld.size }
            .toList()
            .sortedByDescending { (_, count) -> count }

        val header = Component.text()
            .content("--- Worlds with invalid warps: (${warps.size} total) ---")
            .color(NamedTextColor.GOLD)
            .build()
        sender.sendMessage(header)
        warpsByWorld.forEach { (worldId, count) ->
            val worldIdStr = worldId.toString()
            val message = Component.text("-", NamedTextColor.GRAY)
                .append(Component.space())
                .append(Component.text(worldIdStr, NamedTextColor.RED))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text("$count warps", NamedTextColor.WHITE))
            
            if (sender is Player) {
                // For players, add click and hover events
                val clickableMessage = message
                    .clickEvent(ClickEvent.copyToClipboard(worldIdStr))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy world ID to clipboard", NamedTextColor.GREEN)))
                
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
                    sender.sendMessage(Component.text("Error: '$worldId' is not a valid UUID or world name", NamedTextColor.RED))
                    return
                }
                world.uid
            }
            
            val (removed, total) = removeInvalidWarpsForWorld.execute(uuid)
            val world = Bukkit.getWorld(uuid)
            val worldName = world?.name ?: "Unknown World ($uuid)"
            
            val message = Component.text()
                .append(Component.text("Removed ", NamedTextColor.GREEN))
                .append(Component.text(removed, NamedTextColor.WHITE))
                .append(Component.text(" warps from world '", NamedTextColor.GREEN))
                .append(Component.text(worldName, NamedTextColor.YELLOW))
                .append(Component.text("' ", NamedTextColor.GREEN))
                .build()
                
            sender.sendMessage(message)
        } catch (e: Exception) {
            sender.sendMessage(Component.text("An error occurred while removing warps: ${e.message}", NamedTextColor.RED))
        }
    }

    @Subcommand("invalids removeall")
    @CommandPermission("waystonewarps.admin.invalids.removeall")
    @Description("Remove all warps in invalid worlds")
    fun onRemoveAllInvalids(sender: CommandSender) {
        val (removed, total) = removeAllInvalidWarps.execute()
        sender.sendMessage("§aRemoved $removed invalid warps.")
    }
}