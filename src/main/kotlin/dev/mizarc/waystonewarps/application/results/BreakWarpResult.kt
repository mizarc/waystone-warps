package dev.mizarc.waystonewarps.application.results

import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.UUID

sealed class BreakWarpResult {
    data class Success(val warp: Warp) : BreakWarpResult()
    data class Breaking(val breaksRemaining: Int) : BreakWarpResult()
    data object WarpNotFound : BreakWarpResult()
}