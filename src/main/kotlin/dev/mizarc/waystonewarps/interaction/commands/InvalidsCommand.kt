package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import dev.mizarc.waystonewarps.application.actions.management.RemoveInvalidWarps
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

@CommandAlias("waystonewarps|ww")
@CommandPermission("waystonewarps.admin.invalids")
class InvalidsCommand(private val removeInvalidWarps: RemoveInvalidWarps) : BaseCommand() {

    @Subcommand("invalids list")
    @CommandPermission("waystonewarps.admin.invalids.list")
    @Description("List all warps in invalid worlds")
    fun onListInvalids(sender: CommandSender, @Optional @Name("player") targetPlayer: OfflinePlayer?) {
        val warps = if (targetPlayer != null) {
            removeInvalidWarps.listInvalidWarps(targetPlayer.uniqueId)
        } else {
            removeInvalidWarps.listAllInvalidWarps()
        }

        if (warps.isEmpty()) {
            sender.sendMessage("§aNo invalid warps found.")
            return
        }

        sender.sendMessage("§6=== Invalid Warps (${warps.size}) ===")
        warps.forEach { warp ->
            val playerName = Bukkit.getOfflinePlayer(warp.playerId).name ?: "Unknown"
            sender.sendMessage("§7- §f${warp.name} §7(Owner: $playerName, World: ${warp.worldId})")
        }
    }

    @Subcommand("invalids remove")
    @CommandPermission("waystonewarps.admin.invalids.remove")
    @Description("Remove warps in invalid worlds")
    fun onRemoveInvalids(sender: CommandSender, @Optional @Name("player") targetPlayer: OfflinePlayer?) {
        val (removed, total) = if (targetPlayer != null) {
            removeInvalidWarps.execute(targetPlayer.uniqueId)
        } else {
            removeInvalidWarps.execute()
        }

        if (targetPlayer != null) {
            sender.sendMessage("§aRemoved $removed invalid warps for ${targetPlayer.name} (out of $total checked).")
        } else {
            sender.sendMessage("§aRemoved $removed invalid warps (out of $total checked).")
        }
    }
}