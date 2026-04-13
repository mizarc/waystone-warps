package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIconRepository
import java.util.UUID

class RemovePlayerWarpIcon(private val playerWarpIconRepository: PlayerWarpIconRepository) {
    fun execute(playerId: UUID, warpId: UUID) {
        playerWarpIconRepository.remove(playerId, warpId)
    }
}
