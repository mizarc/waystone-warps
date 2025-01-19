package dev.mizarc.waystonewarps.infrastructure.persistence.waystones

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import dev.mizarc.waystonewarps.domain.waystones.WaystoneRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.time.Instant
import java.util.*

class WaystoneRepositorySQLite(private val storage: Storage<Database>): WaystoneRepository {
    private val waystones: MutableMap<UUID, Waystone> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun getAll(): Set<Waystone> {
        return waystones.values.toSet()
    }

    override fun getById(id: UUID): Waystone? {
        return waystones.values.firstOrNull { it.id == id }
    }

    override fun getByPlayer(player: OfflinePlayer): List<Waystone> {
        return waystones.values.filter { it.player == player }
    }

    override fun getByPosition(position: Position3D, worldId: UUID): Waystone? {
        return waystones.values.firstOrNull { it.position == position }
    }

    override fun add(waystone: Waystone) {
        waystones[waystone.id] = waystone
        storage.connection.executeInsert("INSERT INTO waystones (id, playerId, creationTime, name, worldId, " +
                "positionX, positionY, positionZ, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            waystone.id, waystone.player.uniqueId, waystone.creationTime, waystone.name, waystone.worldId,
            waystone.position.x, waystone.position.y, waystone.position.z, waystone.icon.name)
    }

    override fun update(waystone: Waystone) {
        waystones.remove(waystone.id)
        waystones[waystone.id] = waystone
        storage.connection.executeUpdate("UPDATE waystones SET playerId=?, creationTime=?, name=?, worldId=?, " +
                "positionX=?, positionY=?, positionZ=?, icon=? WHERE id=?",
            waystone.player.uniqueId, waystone.creationTime, waystone.name, waystone.worldId, waystone.position.x, waystone.position.y,
            waystone.position.z, waystone.icon.name, waystone.id)
        return
    }

    override fun remove(waystone: Waystone) {
        waystones.remove(waystone.id)
        storage.connection.executeUpdate("DELETE FROM waystones WHERE id=?", waystone.id)
    }

    private fun createTable() {
        storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS waystones (id TEXT NOT NULL, playerId TEXT NOT NULL, " +
                "creationTime TEXT NOT NULL, name TEXT, worldId TEXT, positionX INTEGER, positionY INTEGER, " +
                "positionZ INTEGER, direction INT, icon TEXT);")
    }

    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM waystones;")
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