package dev.mizarc.waystonewarps.application.actions.groups

import dev.mizarc.waystonewarps.domain.warps.WarpGroup
import dev.mizarc.waystonewarps.domain.warps.WarpGroupRepository

class GetAllWarpGroups(private val warpGroupRepository: WarpGroupRepository) {
    fun execute(): List<WarpGroup> = warpGroupRepository.getAll().sortedBy { it.name }
}
