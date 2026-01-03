package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.domain.warps.Warp

interface WarpEventPublisher {
    fun warpCreated(warp: Warp)
    fun warpDeleted(warp: Warp)
    fun warpModified(oldWarp: Warp, newWarp: Warp)
}
