package dev.mizarc.waystonewarps.infrastructure.persistence.waystones

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.waystones.Waystone
import dev.mizarc.waystonewarps.domain.waystones.WaystoneRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import org.bukkit.Material
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

    override fun getByPlayer(playerId: UUID): List<Waystone> {
        return waystones.values.filter { it.playerId == playerId }
    }

    override fun getByPosition(position: Position3D, worldId: UUID): Waystone? {
        return waystones.values.firstOrNull { it.position == position }
    }

    override fun add(waystone: Waystone) {
        waystones[waystone.id] = waystone
        storage.connection.executeInsert("INSERT INTO waystones (id, playerId, creationTime, name, worldId, " +
                "positionX, positionY, positionZ, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            waystone.id, waystone.playerId, waystone.creationTime, waystone.name, waystone.worldId,
            waystone.position.x, waystone.position.y, waystone.position.z, waystone.icon.name)
    }

    override fun update(waystone: Waystone) {
        waystones.remove(waystone.id)
        waystones[waystone.id] = waystone
        storage.connection.executeUpdate("UPDATE waystones SET playerId=?, creationTime=?, name=?, worldId=?, " +
                "positionX=?, positionY=?, positionZ=?, icon=? WHERE id=?",
            waystone.playerId, waystone.creationTime, waystone.name, waystone.worldId, waystone.position.x, waystone.position.y,
            waystone.position.z, waystone.icon.name, waystone.id)
        return
    }

    override fun remove(id: UUID) {
        waystones.remove(id)
        storage.connection.executeUpdate("DELETE FROM waystones WHERE id=?", id)
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
                UUID.fromString(result.getString("playerId")),
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