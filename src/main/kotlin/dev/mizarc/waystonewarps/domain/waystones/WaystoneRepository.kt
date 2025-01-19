package dev.mizarc.waystonewarps.domain.waystones

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.time.Instant
import java.util.*

class WaystoneRepository(private val database: Database) {
    private val waystones: MutableMap<UUID, Waystone> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun getAll(): List<Waystone> {
        return waystones.values.toList()
    }

    fun getById(id: UUID): Waystone? {
        return waystones.values.firstOrNull { it.id == id }
    }

    fun getByPlayer(player: OfflinePlayer): List<Waystone> {
        return waystones.values.filter { it.player == player }
    }

    fun getByPosition(position: Position3D): Waystone? {
        return waystones.values.firstOrNull { it.position == position }
    }

    fun getByName(name: String): Waystone? {
        return waystones.values.firstOrNull {it.name == name }
    }

    fun add(waystone: Waystone) {
        waystones[waystone.id] = waystone
        database.executeInsert("INSERT INTO waystones (id, playerId, creationTime, name, worldId, " +
                "positionX, positionY, positionZ, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            waystone.id, waystone.player.uniqueId, waystone.creationTime, waystone.name, waystone.worldId,
            waystone.position.x, waystone.position.y, waystone.position.z, waystone.icon.name)
    }

    fun update(waystone: Waystone) {
        waystones.remove(waystone.id)
        waystones[waystone.id] = waystone
        database.executeUpdate("UPDATE waystones SET playerId=?, creationTime=?, name=?, worldId=?, " +
                "positionX=?, positionY=?, positionZ=?, icon=? WHERE id=?",
            waystone.player.uniqueId, waystone.creationTime, waystone.name, waystone.worldId, waystone.position.x, waystone.position.y,
            waystone.position.z, waystone.icon.name, waystone.id)
        return
    }

    fun remove(waystone: Waystone) {
        waystones.remove(waystone.id)
        database.executeUpdate("DELETE FROM waystones WHERE id=?", waystone.id)
    }

    private fun createTable() {
        database.executeUpdate("CREATE TABLE IF NOT EXISTS waystones (id TEXT NOT NULL, playerId TEXT NOT NULL, " +
                "creationTime TEXT NOT NULL, name TEXT, worldId TEXT, positionX INTEGER, positionY INTEGER, " +
                "positionZ INTEGER, direction INT, icon TEXT);")
    }

    private fun preload() {
        val results = database.getResults("SELECT * FROM waystones;")
        for (result in results) {
            waystones[UUID.fromString(result.getString("id"))] = Waystone(
                UUID.fromString(result.getString("id")),
                Bukkit.getOfflinePlayer(UUID.fromString(result.getString("playerId"))),
                Instant.parse(result.getString("creationTime")),
                result.getString("name"),
                UUID.fromString(result.getString("worldId")),
                Position3D(
                    result.getInt("positionX"),
                    result.getInt("positionY"),
                    result.getInt("positionZ")),
                Material.valueOf(result.getString("icon")))
        }
    }
}