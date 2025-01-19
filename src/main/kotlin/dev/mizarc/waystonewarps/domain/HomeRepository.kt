package dev.mizarc.waystonewarps.domain

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.Direction
import dev.mizarc.waystonewarps.Position
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.OfflinePlayer
import java.sql.SQLException
import java.time.Instant
import java.util.*

class HomeRepository(private val database: Database) {
    private val homes: MutableMap<UUID, Home> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun getAll(): List<Home> {
        return homes.values.toList()
    }

    fun getByPlayer(player: OfflinePlayer): List<Home> {
        return homes.values.filter { it.player == player }
    }

    fun getByPosition(position: Position): Home? {
        return homes.values.firstOrNull() { it.position == position }
    }

    fun add(home: Home) {
        homes[home.id] = home
        database.executeInsert("INSERT INTO homes (id, playerId, creationTime, name, colour, worldId, " +
                "positionX, positionY, positionZ, direction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            home.id, home.player.uniqueId, home.creationTime, home.name, home.colour, home.worldId,
            home.position.x, home.position.y, home.position.z, home.direction.ordinal)
    }

    fun update(home: Home) {
        homes.remove(home.id)
        homes[home.id] = home
        database.executeUpdate("UPDATE homes SET playerId=?, creationTime=?, name=?, colour=?, worldId=?, " +
                "positionX=?, positionY=?, positionZ=?, direction=? WHERE id=?",
            home.player.uniqueId, home.creationTime, home.name, home.colour, home.worldId,
            home.position.x, home.position.y, home.position.z, home.direction.ordinal, home.id)
        return
    }

    fun remove(home: Home) {
        homes.remove(home.id)
        database.executeUpdate("DELETE FROM homes WHERE id=?", home.id)
    }

    private fun createTable() {
        try {
            database.executeUpdate("CREATE TABLE IF NOT EXISTS homes (id TEXT, playerId TEXT, " +
                    "creationTime TEXT, name TEXT, colour TEXT, worldId TEXT, positionX INTEGER, positionY INTEGER, " +
                    "positionZ INTEGER, direction INT);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    private fun preload() {
        try {
            val results = database.getResults("SELECT * FROM homes;")
            for (result in results) {
                homes[UUID.fromString(result.getString("id"))] = Home(
                    UUID.fromString(result.getString("id")),
                    Bukkit.getOfflinePlayer(UUID.fromString(result.getString("playerId"))),
                    Instant.parse(result.getString("creationTime")),
                    result.getString("name"),
                    DyeColor.valueOf(result.getString("colour")),
                    UUID.fromString(result.getString("worldId")),
                    Position(
                        result.getInt("positionX"),
                        result.getInt("positionY"),
                        result.getInt("positionZ")),
                    Direction.values()[result.getInt("direction")]
                )
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}