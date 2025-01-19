package dev.mizarc.waystonewarps.interaction.listeners

import net.milkbowl.vault.chat.Chat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import dev.mizarc.waystonewarps.*
import dev.mizarc.waystonewarps.domain.players.PlayerStateRepository
import dev.mizarc.waystonewarps.infrastructure.services.playerlimit.VaultPlayerLimitServiceImpl

class PlayerRegistrationListener(val homes: HomeRepository, val players: PlayerStateRepository,
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