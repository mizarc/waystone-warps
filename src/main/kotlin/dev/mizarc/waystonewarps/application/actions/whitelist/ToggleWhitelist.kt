package dev.mizarc.waystonewarps.application.actions.whitelist

import dev.mizarc.waystonewarps.domain.whitelist.Whitelist
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import java.util.UUID

class ToggleWhitelist(private val whitelistRepository: WhitelistRepository) {

    fun execute(warpId: UUID, playerId: UUID): Boolean {
        if (whitelistRepository.isWhitelisted(warpId, playerId)) {
            whitelistRepository.remove(warpId, playerId)
            return false
        }
        whitelistRepository.add(Whitelist(warpId, playerId))
        return true
    }
}