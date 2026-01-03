package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.PlayerLocaleService
import org.bukkit.Bukkit
import java.util.*

class PlayerLocaleServicePaper: PlayerLocaleService {
    override fun getLocale(playerId: UUID): String {
        val player = Bukkit.getPlayer(playerId) ?: return ""
        return player.locale().toString()
    }
}