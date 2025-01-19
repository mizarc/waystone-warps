package dev.mizarc.worldwidewarps.domain

import co.aikar.idb.Database
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

class \WarpAccessRepository(private val database: Database, private val warpRepository: WarpRepository) {
    private val playerAccesses: MutableMap<UUID, MutableSet<Warp>> = mutableMapOf()
    private val warpPlayers: MutableMap<UUID, MutableSet<OfflinePlayer>> = mutableMapOf()

    fun init() {
        database.executeUpdate("CREATE TABLE IF NOT EXISTS warp_access (id TEXT, playerId TEXT, warpId TEXT);")

        val results = database.getResults("SELECT * FROM warp_access;")
        for (result in results) {
            val warp = warpRepository.getById(UUID.fromString(result.getString("warpId"))) ?: continue
            val player = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("playerId")))
            playerAccesses.getOrPut(player.uniqueId) { mutableSetOf() }.add(warp)
            warpPlayers.getOrPut(warp.id) { mutableSetOf() }.add(player)
        }
    }

    fun hasAccess(player: OfflinePlayer, warp: Warp): Boolean {
        return warpPlayers[warp.id]?.toList()?.contains(player) ?: false
    }

    fun getByPlayer(player: OfflinePlayer): List<Warp> {
        return playerAccesses[player.uniqueId]?.toList() ?: return listOf()
    }

    fun getByWarp(warp: Warp): List<OfflinePlayer> {
        return warpPlayers[warp.id]?.toList() ?: return listOf()
    }

    fun addWarpForPlayer(player: OfflinePlayer, warp: Warp) {
        playerAccesses.getOrPut(player.uniqueId) { mutableSetOf() }.add(warp)
        warpPlayers.getOrPut(warp.id) { mutableSetOf() }.add(player)
        database.executeInsert("INSERT INTO warp_access (id, playerId, warpId) VALUES (?, ?, ?)",
            UUID.randomUUID(), player.uniqueId, warp.id)
    }

    fun removeWarpForPlayer(player: OfflinePlayer, warp: Warp) {
        playerAccesses[player.uniqueId]?.remove(warp) ?: return
        warpPlayers[warp.id]?.remove(player) ?: return
        database.executeUpdate("DELETE FROM warp_access WHERE playerId=? AND warpId=?", player.uniqueId, warp.id)
    }

    fun removeAllAccess(warp: Warp) {
        val foundWarpPlayers = warpPlayers[warp.id] ?: return
        for (player in foundWarpPlayers) {
            val foundPlayer = playerAccesses[player.uniqueId] ?: continue
            foundPlayer.remove(warp)
            database.executeUpdate("DELETE FROM warp_access WHERE playerId=? AND warpId=?", player.uniqueId, warp.id)
        }

        warpPlayers.remove(warp.id)
    }
}