package dev.mizarc.waystonewarps.application.actions.management

import IconMeta
import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIcon
import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIconRepository
import java.util.UUID

class SetPlayerWarpIcon(private val playerWarpIconRepository: PlayerWarpIconRepository) {
    fun execute(playerId: UUID, warpId: UUID, materialName: String, iconMeta: IconMeta = IconMeta()) {
        playerWarpIconRepository.set(PlayerWarpIcon(playerId, warpId, materialName, iconMeta))
    }
}
