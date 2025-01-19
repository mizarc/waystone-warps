package dev.mizarc.waystonewarps.domain

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.Direction
import dev.mizarc.waystonewarps.Position
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.time.Instant
import java.util.*

class WarpRepository(private val database: Database) {
    private val warps: MutableMap<UUID, Warp> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun getAll(): List<Warp> {
        return warps.values.toList()
    }

    fun getById(id: UUID): Warp? {
        return warps.values.firstOrNull { it.id == id }
    }

    fun getByPlayer(player: OfflinePlayer): List<Warp> {
        return warps.values.filter { it.player == player }
    }

    fun getByPosition(position: Position): Warp? {
        return warps.values.firstOrNull { it.position == position }
    }

    fun getByName(name: String): Warp? {
        return warps.values.firstOrNull {it.name == name }
    }

    fun add(warp: Warp) {
        warps[warp.id] = warp
        database.executeInsert("INSERT INTO warps (id, playerId, creationTime, name, worldId, " +
                "positionX, positionY, positionZ, direction, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            warp.id, warp.player.uniqueId, warp.creationTime, warp.name, warp.worldId,
            warp.position.x, warp.position.y, warp.position.z, warp.direction.ordinal, warp.icon.name)
    }

    fun update(warp: Warp) {
        warps.remove(warp.id)
        warps[warp.id] = warp
        database.executeUpdate("UPDATE warps SET playerId=?, creationTime=?, name=?, worldId=?, " +
                "positionX=?, positionY=?, positionZ=?, direction=?, icon=? WHERE id=?",
            warp.player.uniqueId, warp.creationTime, warp.name, warp.worldId, warp.position.x, warp.position.y,
            warp.position.z, warp.direction.ordinal, warp.icon.name, warp.id)
        return
    }

    fun remove(warp: Warp) {
        warps.remove(warp.id)
        database.executeUpdate("DELETE FROM warps WHERE id=?", warp.id)
    }

    private fun createTable() {
        database.executeUpdate("CREATE TABLE IF NOT EXISTS warps (id TEXT NOT NULL, playerId TEXT NOT NULL, " +
                "creationTime TEXT NOT NULL, name TEXT, worldId TEXT, positionX INTEGER, positionY INTEGER, " +
                "positionZ INTEGER, direction INT, icon TEXT);")
    }

    private fun preload() {
        val results = database.getResults("SELECT * FROM warps;")
        for (result in results) {
            warps[UUID.fromString(result.getString("id"))] = Warp(
                UUID.fromString(result.getString("id")),
                Bukkit.getOfflinePlayer(UUID.fromString(result.getString("playerId"))),
                Instant.parse(result.getString("creationTime")),
                result.getString("name"),
                UUID.fromString(result.getString("worldId")),
                Position(
                    result.getInt("positionX"),
                    result.getInt("positionY"),
                    result.getInt("positionZ")),
                Direction.values()[result.getInt("direction")],
                Material.valueOf(result.getString("icon")))
        }
    }
}