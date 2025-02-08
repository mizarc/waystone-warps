package dev.mizarc.waystonewarps.application.actions.player

import dev.mizarc.waystonewarps.domain.playerstate.PlayerState
import dev.mizarc.waystonewarps.domain.playerstate.PlayerStateRepository
import java.util.UUID

class GetPlayerState(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): PlayerState? {
        val playerState = playerStateRepository.getById(playerId) ?: return null
        return playerState
    }
}