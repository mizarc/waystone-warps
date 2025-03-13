package dev.mizarc.waystonewarps.infrastructure.persistence.discoveries

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.discoveries.Discovery
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import java.sql.SQLException
import java.time.Instant
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

    override fun getByWarpAndPlayer(warpId: UUID, playerId: UUID): Discovery? {
        return discoveries[playerId]?.find { it.warpId == warpId }
    }

    override fun add(discovery: Discovery) {
        val playerDiscoveries = discoveries.getOrPut(discovery.playerId) { mutableSetOf() }
        playerDiscoveries.add(discovery)

        try {
            storage.connection.executeUpdate("INSERT INTO discoveries (warpId, playerId, discoveredTime, " +
                    "lastVisitedTime, isFavourite) VALUES (?,?,?,?,?)",
                discovery.warpId, discovery.playerId, discovery.discoveredTime,
                discovery.lastVisitedTime, discovery.isFavourite)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun update(discovery: Discovery) {
        val playerDiscoveries = discoveries[discovery.playerId] ?: return
        // Find the existing discovery in the set
        val existingDiscovery = playerDiscoveries.find { it.warpId == discovery.warpId }
        if (existingDiscovery != null) {
            // Remove the old entry and add the updated one
            playerDiscoveries.remove(existingDiscovery)
            playerDiscoveries.add(discovery)
        } else {
            // Add the new discovery if it doesn't already exist
            playerDiscoveries.add(discovery)
        }

        storage.connection.executeUpdate("UPDATE discoveries SET lastVisitedTime=?, isFavourite=? " +
                "WHERE warpId=? AND playerId=?",
            discovery.lastVisitedTime, discovery.isFavourite, discovery.warpId, discovery.playerId)
        return
    }

    override fun remove(warpId: UUID, playerId: UUID) {
        val playerDiscoveries = discoveries[playerId]
        playerDiscoveries?.removeIf { it.warpId == warpId }

        try {
            storage.connection.executeUpdate("DELETE FROM discoveries WHERE warpId=? AND playerId=?",
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
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS discoveries (warpId TEXT, " +
                    "playerId TEXT, discoveredTime TEXT, lastVisitedTime TEXT, isFavourite INTEGER," +
                    "PRIMARY KEY(warpId, playerId));")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all warp discoveries from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM discoveries")
        for (result in results) {
            val warpId = UUID.fromString(result.getString("warpId"))
            val playerId = UUID.fromString(result.getString("playerId"))
            val discoveredTime = Instant.parse(result.getString("discoveredTime"))
            val lastVisitedTime = Instant.parse(result.getString("lastVisitedTime"))
            val isFavourite = result.getInt("isFavourite") != 0
            try {
                val discovery = Discovery(warpId, playerId, discoveredTime, lastVisitedTime, isFavourite)
                val foundDiscoveries = discoveries.getOrPut(playerId) { mutableSetOf(discovery) }
                foundDiscoveries.add(discovery)
            }
            catch (error: IllegalArgumentException) {
                continue
            }
        }
    }
}
