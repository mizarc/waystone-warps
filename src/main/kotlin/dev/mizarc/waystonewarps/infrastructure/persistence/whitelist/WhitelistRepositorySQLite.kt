package dev.mizarc.waystonewarps.infrastructure.persistence.whitelist

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.whitelist.Whitelist
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import java.util.UUID

class WhitelistRepositorySQLite(private val storage: Storage<Database>): WhitelistRepository {
    private val whitelistMap = HashMap<UUID, MutableSet<UUID>>()

    init {
        createTable()
        preload()
    }

    override fun getByWarp(warpId: UUID): List<UUID> {
        return whitelistMap[warpId]?.toList() ?: emptyList()
    }

    override fun getByPlayer(playerId: UUID): List<UUID> {
        return whitelistMap.entries
            .filter { (_, players) -> players.contains(playerId) }
            .map { (warpId, _) -> warpId }
            .toList()
    }

    override fun add(whitelist: Whitelist) {
        whitelistMap.computeIfAbsent(whitelist.warpId) { HashSet() }.add(whitelist.playerId)
    }

    override fun remove(warpId: UUID, playerId: UUID) {
        whitelistMap[warpId]?.remove(playerId)
        if (whitelistMap[warpId]?.isEmpty() == true) {
            whitelistMap.remove(warpId)
        }
    }

    override fun removeByWarp(warpId: UUID) {
        whitelistMap.remove(warpId)
    }

    private fun createTable() {
        storage.connection.executeUpdate("""
            CREATE TABLE IF NOT EXISTS whitelist (
                warp_id UUID NOT NULL,
                player_id UUID NOT NULL,
                PRIMARY KEY (warp_id, player_id)
            );"""
        )
    }

    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM whitelist;")
        for (result in results) {
            val warpId = UUID.fromString(result.getString("warp_id"))
            val playerId = UUID.fromString(result.getString("player_id"))
            whitelistMap.computeIfAbsent(warpId) { HashSet() }.add(playerId)
        }
    }
}