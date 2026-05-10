package dev.mizarc.waystonewarps.application.actions.management

import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

enum class AssignWarpGroupResult { SUCCESS, WARP_NOT_FOUND, GROUP_NOT_FOUND, NOT_AUTHORIZED }

class AssignWarpGroup(
    private val warpRepository: WarpRepository,
    private val warpGroupRepository: WarpGroupRepository
) {
    fun execute(editorPlayerId: UUID, warpId: UUID, groupId: UUID?, bypassOwnership: Boolean = false): AssignWarpGroupResult {
        val warp = warpRepository.getById(warpId) ?: return AssignWarpGroupResult.WARP_NOT_FOUND
        if (warp.playerId != editorPlayerId && !bypassOwnership) return AssignWarpGroupResult.NOT_AUTHORIZED
        if (groupId != null && warpGroupRepository.getById(groupId) == null) return AssignWarpGroupResult.GROUP_NOT_FOUND
        warp.groupId = groupId
        warpRepository.update(warp)
        return AssignWarpGroupResult.SUCCESS
    }
}
