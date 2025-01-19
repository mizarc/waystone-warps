package dev.mizarc.worldwidewarps.listeners

import net.milkbowl.vault.chat.Chat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import dev.mizarc.worldwidewarps.*
import dev.mizarc.worldwidewarps.domain.HomeRepository
import dev.mizarc.worldwidewarps.domain.PlayerRepository

class PlayerRegistrationListener(val homes: HomeRepository, val players: PlayerRepository,
                                 val config: Config, val metadata: Chat): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerState = PlayerState(event.player, config, metadata)
        players.add(playerState)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        players.remove(event.player.uniqueId)
    }
}