package dev.mizarc.waystonewarps.application.actions.groups

import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class DeleteWarpGroup(
    private val warpGroupRepository: WarpGroupRepository,
    private val warpRepository: WarpRepository
) {
    fun execute(groupId: UUID) {
        warpRepository.getAll()
            .filter { it.groupId == groupId }
            .forEach { warp ->
                warp.groupId = null
                warpRepository.update(warp)
            }
        warpGroupRepository.remove(groupId)
    }
}
