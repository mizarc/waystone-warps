package dev.mizarc.waystonewarps.application.actions.groups

import dev.mizarc.waystonewarps.domain.warps.WarpGroup
import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository
import java.util.UUID

enum class CreateWarpGroupResult { SUCCESS, NAME_BLANK, NAME_TAKEN }

class CreateWarpGroup(private val warpGroupRepository: WarpGroupRepository) {
    fun execute(creatorId: UUID, name: String): CreateWarpGroupResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return CreateWarpGroupResult.NAME_BLANK
        if (warpGroupRepository.getByName(trimmed) != null) return CreateWarpGroupResult.NAME_TAKEN
        warpGroupRepository.add(WarpGroup(UUID.randomUUID(), creatorId, trimmed))
        return CreateWarpGroupResult.SUCCESS
    }
}
