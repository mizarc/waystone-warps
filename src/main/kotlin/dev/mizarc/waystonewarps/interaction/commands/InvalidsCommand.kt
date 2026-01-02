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
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("waystonewarps|ww")
@CommandPermission("waystonewarps.admin.invalids")
class InvalidsCommand(
    private val listInvalidWarps: ListInvalidWarps,
    private val removeAllInvalidWarps: RemoveAllInvalidWarps,
    private val removeInvalidWarpsForWorld: RemoveInvalidWarpsForWorld
) : BaseCommand() {

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
            .content("--- Invalid Warps (${warps.size} total) ---")
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
    fun onRemoveInvalids(sender: CommandSender, @Name("world") worldId: UUID) {
        val (removed, total) = removeInvalidWarpsForWorld.execute(worldId)
        val worldName = Bukkit.getWorld(worldId)?.name ?: worldId.toString()
        sender.sendMessage("§aRemoved $removed warps from world '$worldName' (out of $total checked).")
    }

    @Subcommand("invalids removeall")
    @CommandPermission("waystonewarps.admin.invalids.removeall")
    @Description("Remove all warps in invalid worlds")
    fun onRemoveAllInvalids(sender: CommandSender) {
        val (removed, total) = removeAllInvalidWarps.execute()
        sender.sendMessage("§aRemoved $removed invalid warps (out of $total checked).")
    }
}