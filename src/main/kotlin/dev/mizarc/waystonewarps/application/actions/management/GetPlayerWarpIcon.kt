package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIcon
import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIconRepository
import java.util.UUID

class GetPlayerWarpIcon(private val playerWarpIconRepository: PlayerWarpIconRepository) {
    fun execute(playerId: UUID, warpId: UUID): PlayerWarpIcon? =
        playerWarpIconRepository.getByPlayerAndWarp(playerId, warpId)
}
