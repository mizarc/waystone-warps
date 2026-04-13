package dev.mizarc.waystonewarps.domain.warps

import java.util.UUID

interface PlayerWarpIconRepository {
    fun getByPlayerAndWarp(playerId: UUID, warpId: UUID): PlayerWarpIcon?
    fun getByPlayer(playerId: UUID): List<PlayerWarpIcon>
    fun set(entry: PlayerWarpIcon)
    fun remove(playerId: UUID, warpId: UUID)
}
