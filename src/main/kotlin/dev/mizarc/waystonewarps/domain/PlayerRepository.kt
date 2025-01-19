package dev.mizarc.waystonewarps.domain

import dev.mizarc.waystonewarps.PlayerState
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class PlayerRepository {
    var playerStates: ArrayList<PlayerState> = ArrayList()

    fun getAll(): ArrayList<PlayerState> {
        return playerStates
    }

    fun getByPlayer(player: Player) : PlayerState? {
        for (playerState in playerStates) {
            if (playerState.player.uniqueId == player.uniqueId) {
                return playerState
            }
        }

        return null
    }

    fun add(playerState: PlayerState) : Boolean {
        for (existingPlayerState in playerStates) {
            if (existingPlayerState.player.uniqueId == playerState.player.uniqueId) {
                return false
            }
        }
        playerStates.add(playerState)
        return true
    }

    fun remove(playerId: UUID) : Boolean {
        for (playerState in playerStates) {
            if (playerState.player.uniqueId == playerId) {
                playerStates.remove(playerState)
                return true
            }
        }
        return false
    }
}