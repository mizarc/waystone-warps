package dev.mizarc.waystonewarps.infrastructure.persistence.groups

import co.aikar.idb.Database
import dev.mizarc.waystonewarps.domain.warps.WarpGroup
import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository
import dev.mizarc.waystonewarps.infrastructure.persistence.storage.Storage
import java.util.UUID

class WarpGroupRepositorySQLite(private val storage: Storage<Database>) : WarpGroupRepository {
    private val groups: MutableMap<UUID, WarpGroup> = mutableMapOf()

    init {
        preload()
    }

    override fun getAll(): List<WarpGroup> = groups.values.toList()

    override fun getById(id: UUID): WarpGroup? = groups[id]

    override fun getByName(name: String): WarpGroup? =
        groups.values.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override fun add(group: WarpGroup) {
        groups[group.id] = group
        storage.connection.executeInsert(
            "INSERT INTO warp_groups (id, name, createdBy) VALUES (?, ?, ?);",
            group.id, group.name, group.createdBy
        )
    }

    override fun update(group: WarpGroup) {
        groups[group.id] = group
        storage.connection.executeUpdate(
            "UPDATE warp_groups SET name=?, createdBy=? WHERE id=?",
            group.name, group.createdBy, group.id
        )
    }

    override fun remove(id: UUID) {
        groups.remove(id)
        storage.connection.executeUpdate("DELETE FROM warp_groups WHERE id=?", id)
    }

    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM warp_groups;")
        for (result in results) {
            val group = WarpGroup(
                UUID.fromString(result.getString("id")),
                UUID.fromString(result.getString("createdBy")),
                result.getString("name")
            )
            groups[group.id] = group
        }
    }
}
