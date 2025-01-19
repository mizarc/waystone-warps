package dev.mizarc.waystonewarps.listeners

import net.milkbowl.vault.chat.Chat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import dev.mizarc.waystonewarps.*
import dev.mizarc.waystonewarps.domain.HomeRepository
import dev.mizarc.waystonewarps.domain.PlayerRepository
import dev.mizarc.waystonewarps.infrastructure.services.playerlimit.VaultPlayerLimitServiceImpl

class PlayerRegistrationListener(val homes: HomeRepository, val players: PlayerRepository,
                                 val config: Config, val metadata: Chat): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val vaultPlayerLimitServiceImpl = VaultPlayerLimitServiceImpl(event.player, config, metadata)
        players.add(vaultPlayerLimitServiceImpl)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        players.remove(event.player.uniqueId)
    }
}