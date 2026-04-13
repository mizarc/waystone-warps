package dev.mizarc.waystonewarps.infrastructure.persistence.playericons

import IconMeta
import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIcon
import dev.mizarc.waystonewarps.domain.warps.PlayerWarpIconRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import kotlinx.serialization.json.Json
import java.util.UUID

class PlayerWarpIconRepositorySQLite(private val storage: Storage<Database>) : PlayerWarpIconRepository {
    private val icons: MutableMap<Pair<UUID, UUID>, PlayerWarpIcon> = mutableMapOf()

    private val iconMetaJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    init {
        preload()
    }

    override fun getByPlayerAndWarp(playerId: UUID, warpId: UUID): PlayerWarpIcon? =
        icons[Pair(playerId, warpId)]

    override fun getByPlayer(playerId: UUID): List<PlayerWarpIcon> =
        icons.values.filter { it.playerId == playerId }

    override fun set(entry: PlayerWarpIcon) {
        icons[Pair(entry.playerId, entry.warpId)] = entry
        val iconMetaJsonString = iconMetaJson.encodeToString(entry.iconMeta)
        storage.connection.executeInsert(
            "INSERT OR REPLACE INTO player_warp_icons (playerId, warpId, icon, iconMeta) VALUES (?, ?, ?, ?);",
            entry.playerId, entry.warpId, entry.icon, iconMetaJsonString
        )
    }

    override fun remove(playerId: UUID, warpId: UUID) {
        icons.remove(Pair(playerId, warpId))
        storage.connection.executeUpdate(
            "DELETE FROM player_warp_icons WHERE playerId=? AND warpId=?",
            playerId, warpId
        )
    }

    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM player_warp_icons;")
        for (result in results) {
            val iconMeta = runCatching {
                val raw = result.getString("iconMeta")
                if (raw.isNullOrBlank()) IconMeta() else iconMetaJson.decodeFromString<IconMeta>(raw)
            }.getOrElse { IconMeta() }
            val entry = PlayerWarpIcon(
                UUID.fromString(result.getString("playerId")),
                UUID.fromString(result.getString("warpId")),
                result.getString("icon"),
                iconMeta
            )
            icons[Pair(entry.playerId, entry.warpId)] = entry
        }
    }
}
