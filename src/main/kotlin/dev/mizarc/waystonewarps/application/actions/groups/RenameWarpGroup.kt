package dev.mizarc.waystonewarps.application.actions.groups

import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository
import java.util.UUID

enum class RenameWarpGroupResult { SUCCESS, NAME_BLANK, NAME_TAKEN, NOT_FOUND }

class RenameWarpGroup(private val warpGroupRepository: WarpGroupRepository) {
    fun execute(groupId: UUID, name: String): RenameWarpGroupResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return RenameWarpGroupResult.NAME_BLANK
        val group = warpGroupRepository.getById(groupId) ?: return RenameWarpGroupResult.NOT_FOUND
        val existing = warpGroupRepository.getByName(trimmed)
        if (existing != null && existing.id != groupId) return RenameWarpGroupResult.NAME_TAKEN
        group.name = trimmed
        warpGroupRepository.update(group)
        return RenameWarpGroupResult.SUCCESS
    }
}
