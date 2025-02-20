package dev.mizarc.waystonewarps.application.actions.whitelist

import dev.mizarc.waystonewarps.domain.whitelist.Whitelist
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import java.util.UUID

class AddToWhitelist(private val whitelistRepository: WhitelistRepository) {

    fun execute(warpId: UUID, playerId: UUID): Result<Unit> {
        if (whitelistRepository.isWhitelisted(warpId, playerId)) return Result.failure(Exception("Already whitelisted"))
        whitelistRepository.add(Whitelist(warpId, playerId))
        return Result.success(Unit)
    }
}