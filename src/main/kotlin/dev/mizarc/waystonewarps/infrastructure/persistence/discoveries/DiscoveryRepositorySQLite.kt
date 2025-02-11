package dev.mizarc.waystonewarps.infrastructure.persistence.discoveries

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.discoveries.Discovery
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*

class DiscoveryRepositorySQLite(private val storage: Storage<Database>): DiscoveryRepository {
    private val discoveries: MutableMap<UUID, MutableSet<Discovery>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun getByWarp(warpId: UUID): Set<Discovery> {
        val discoverySet: MutableSet<Discovery> = mutableSetOf()

        for ((_, discoveriesSet) in discoveries) {
            for (discovery in discoveriesSet) {
                if (discovery.warpId == warpId) {
                    discoverySet.add(discovery)
                }
            }
        }
        return discoverySet
    }

    override fun getByPlayer(playerId: UUID): Set<Discovery> {
        return discoveries[playerId] ?: return emptySet()
    }

    override fun add(discovery: Discovery) {
        val playerDiscoveries = discoveries.getOrPut(discovery.playerId) { mutableSetOf() }
        playerDiscoveries.add(discovery)

        try {
            storage.connection.executeUpdate("INSERT INTO discoveries (waystoneId, playerId, firstDiscoveredTime, " +
                    "lastVisitedTime, isFavourite) VALUES (?,?,?,?,?)",
                discovery.warpId, discovery.playerId, discovery.firstDiscoveredTime,
                discovery.lastVisitedTime, discovery.isFavourite)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(playerId: UUID, warpId: UUID) {
        val playerDiscoveries = discoveries[playerId]
        playerDiscoveries?.removeIf { it.warpId == warpId }

        try {
            storage.connection.executeUpdate("REMOVE FROM discoveries WHERE waystoneId=? AND playerId=?",
                warpId, playerId)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store discoveries data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS discoveries (waystoneId TEXT, " +
                    "playerId TEXT, firstDiscoveredTime TEXT, lastVisitedTime TEXT, isFavourite INTEGER);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all waystone discoveries from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM discoveries")
        for (result in results) {
            val waystoneId = UUID.fromString(result.getString("waystoneId"))
            val playerId = UUID.fromString(result.getString("playerId"))
            val firstDiscoveredTime = LocalDateTime.parse(result.getString("firstDiscoveredTime"))
            val lastVisitedTime = LocalDateTime.parse(result.getString("lastVisitedTime"))
            val isFavourite = result.getInt("isFavourite") != 0
            try {
                val discovery = Discovery(waystoneId, playerId, firstDiscoveredTime, lastVisitedTime, isFavourite)
                val foundDiscoveries = discoveries.getOrPut(playerId) { mutableSetOf(discovery) }
                foundDiscoveries.add(discovery)
            }
            catch (error: IllegalArgumentException) {
                continue
            }
        }
    }
}
