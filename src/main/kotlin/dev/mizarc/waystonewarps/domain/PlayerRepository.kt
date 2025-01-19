package dev.mizarc.waystonewarps.domain

import dev.mizarc.waystonewarps.infrastructure.services.playerlimit.VaultPlayerLimitServiceImpl
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class PlayerRepository {
    var vaultPlayerLimitServiceImpls: ArrayList<VaultPlayerLimitServiceImpl> = ArrayList()

    fun getAll(): ArrayList<VaultPlayerLimitServiceImpl> {
        return vaultPlayerLimitServiceImpls
    }

    fun getByPlayer(player: Player) : VaultPlayerLimitServiceImpl? {
        for (playerState in vaultPlayerLimitServiceImpls) {
            if (playerState.player.uniqueId == player.uniqueId) {
                return playerState
            }
        }

        return null
    }

    fun add(vaultPlayerLimitServiceImpl: VaultPlayerLimitServiceImpl) : Boolean {
        for (existingPlayerState in vaultPlayerLimitServiceImpls) {
            if (existingPlayerState.player.uniqueId == vaultPlayerLimitServiceImpl.player.uniqueId) {
                return false
            }
        }
        vaultPlayerLimitServiceImpls.add(vaultPlayerLimitServiceImpl)
        return true
    }

    fun remove(playerId: UUID) : Boolean {
        for (playerState in vaultPlayerLimitServiceImpls) {
            if (playerState.player.uniqueId == playerId) {
                vaultPlayerLimitServiceImpls.remove(playerState)
                return true
            }
        }
        return false
    }
}