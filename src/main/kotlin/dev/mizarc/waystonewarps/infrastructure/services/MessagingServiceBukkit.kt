package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.MessagingService
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import java.util.*

class MessagingServiceBukkit: MessagingService {
    override fun sendChatMessage(playerId: UUID, message: String): Result<Unit> {
        val player = Bukkit.getPlayer(playerId) ?: return Result.failure(Exception("Player not found"))
        player.sendMessage(message)
        return Result.success(Unit)
    }

    override fun sendActionMessage(playerId: UUID, message: String): Result<Unit> {
        val player = Bukkit.getPlayer(playerId) ?: return Result.failure(Exception("Player not found"))
        player.sendActionBar(Component.text(message))
        return Result.success(Unit)
    }

    override fun sendTitleMessage(playerId: UUID, titleText: String, subtitleText: String): Result<Unit> {
        val player = Bukkit.getPlayer(playerId) ?: return Result.failure(Exception("Player not found"))
        val title = Title.title(Component.text(titleText), Component.text(subtitleText))
        player.showTitle(title)
        return Result.success(Unit)
    }

}