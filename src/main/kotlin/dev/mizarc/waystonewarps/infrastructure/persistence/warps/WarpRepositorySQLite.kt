package dev.mizarc.waystonewarps.infrastructure.persistence.warps

import IconMeta
import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.*

class WarpRepositorySQLite(private val storage: Storage<Database>): WarpRepository {
    private val warps: MutableMap<UUID, Warp> = mutableMapOf()

    private val iconMetaJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    init {
        createTable()
        preload()
    }

    override fun getAll(): Set<Warp> {
        return warps.values.toSet()
    }

    override fun getById(id: UUID): Warp? {
        return warps.values.firstOrNull { it.id == id }
    }

    override fun getByPlayer(playerId: UUID): List<Warp> {
        return warps.values.filter { it.playerId == playerId }
    }

    override fun getByName(playerId: UUID, name: String): Warp? {
        return warps.values.find { it.playerId == playerId && it.name.equals(name, ignoreCase = true) }
    }

    override fun getByPosition(position: Position3D, worldId: UUID): Warp? {
        return warps.values.firstOrNull { it.position == position }
    }

    override fun add(warp: Warp) {
        warps[warp.id] = warp
        val iconMetaJsonString = iconMetaJson.encodeToString(warp.iconMeta)
        storage.connection.executeInsert("INSERT INTO warps (id, playerId, creationTime, name, worldId, " +
                "positionX, positionY, positionZ, icon, iconMeta, block, isLocked) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            warp.id, warp.playerId, warp.creationTime, warp.name, warp.worldId,
            warp.position.x, warp.position.y, warp.position.z, warp.icon, iconMetaJsonString, warp.block, warp.isLocked)
    }

    override fun update(warp: Warp) {
        warps.remove(warp.id)
        warps[warp.id] = warp
        val iconMetaJsonString = iconMetaJson.encodeToString(warp.iconMeta)
        storage.connection.executeUpdate("UPDATE warps SET playerId=?, creationTime=?, name=?, worldId=?, " +
                "positionX=?, positionY=?, positionZ=?, icon=?, iconMeta=?, block=?, isLocked=? WHERE id=?",
            warp.playerId, warp.creationTime, warp.name, warp.worldId, warp.position.x, warp.position.y,
            warp.position.z, warp.icon, iconMetaJsonString, warp.block, warp.isLocked, warp.id)
        return
    }

    override fun remove(id: UUID) {
        warps.remove(id)
        storage.connection.executeUpdate("DELETE FROM warps WHERE id=?", id)
    }

    private fun createTable() {
        storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS warps (id TEXT NOT NULL, " +
                "playerId TEXT NOT NULL, creationTime TEXT NOT NULL, name TEXT, worldId TEXT NOT NULL, " +
                "positionX INTEGER NOT NULL, positionY INTEGER NOT NULL, positionZ INTEGER NOT NULL, icon TEXT, iconMeta TEXT," +
                "block TEXT, isLocked INTEGER);")
    }

    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM warps;")

        for (result in results) {
            val iconMeta = runCatching {
                val raw = result.getString("iconMeta")
                println(raw)
                if (raw.isNullOrBlank()) IconMeta() else iconMetaJson.decodeFromString<IconMeta>(raw)
            }.getOrElse { ex ->
                ex.printStackTrace()
                IconMeta()
            }
            println("preloading")
            println(iconMeta)

            warps[UUID.fromString(result.getString("id"))] = Warp(
                UUID.fromString(result.getString("id")),
                UUID.fromString(result.getString("playerId")),
                Instant.parse(result.getString("creationTime")),
                result.getString("name"),
                UUID.fromString(result.getString("worldId")),
                Position3D(
                    result.getInt("positionX"),
                    result.getInt("positionY"),
                    result.getInt("positionZ")),
                result.getString("icon"),
                iconMeta,
                result.getString("block"),
                result.getInt("isLocked") != 0)
        }
    }
}