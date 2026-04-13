package dev.mizarc.waystonewarps.domain.warps

import java.util.UUID

interface WarpGroupRepository {
    fun getAll(): List<WarpGroup>
    fun getById(id: UUID): WarpGroup?
    fun getByName(name: String): WarpGroup?
    fun add(group: WarpGroup)
    fun update(group: WarpGroup)
    fun remove(id: UUID)
}
