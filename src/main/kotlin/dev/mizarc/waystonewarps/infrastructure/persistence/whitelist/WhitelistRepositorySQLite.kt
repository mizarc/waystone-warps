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

    override fun isWhitelisted(warpId: UUID, playerId: UUID): Boolean {
        return whitelistMap[warpId]?.contains(playerId) == true
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
        storage.connection.executeInsert("INSERT INTO whitelist (warpId, playerId, creationTime) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP);",
            whitelist.warpId, whitelist.playerId)
    }

    override fun remove(warpId: UUID, playerId: UUID) {
        whitelistMap[warpId]?.remove(playerId)
        if (whitelistMap[warpId]?.isEmpty() == true) {
            whitelistMap.remove(warpId)
        }
        storage.connection.executeUpdate("DELETE FROM whitelist WHERE warpId=? AND playerId=?", warpId, playerId)
    }

    override fun removeByWarp(warpId: UUID) {
        whitelistMap.remove(warpId)
        storage.connection.executeUpdate("DELETE FROM whitelist WHERE warpId=?", warpId)
    }

    private fun createTable() {
        storage.connection.executeUpdate("""
            CREATE TABLE IF NOT EXISTS whitelist (
                warpId TEXT NOT NULL,
                playerId TEXT NOT NULL,
                creationTime NUMERIC NOT NULL,
                PRIMARY KEY (warpId, playerId)
            );"""
        )
    }

    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM whitelist;")
        for (result in results) {
            val warpId = UUID.fromString(result.getString("warpId"))
            val playerId = UUID.fromString(result.getString("playerId"))
            whitelistMap.computeIfAbsent(warpId) { HashSet() }.add(playerId)
        }
    }
}