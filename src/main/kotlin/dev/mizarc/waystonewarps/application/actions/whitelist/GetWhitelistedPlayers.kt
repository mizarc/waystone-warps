package dev.mizarc.waystonewarps.application.actions.whitelist

import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import java.util.UUID

class GetWhitelistedPlayers(private val whitelistRepository: WhitelistRepository) {
    fun execute(warpId: UUID): List<UUID> {
        return whitelistRepository.getByWarp(warpId)
    }
}