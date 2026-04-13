package dev.mizarc.waystonewarps.domain.warps

import IconMeta
import java.util.UUID

data class PlayerWarpIcon(
    val playerId: UUID,
    val warpId: UUID,
    val icon: String,
    val iconMeta: IconMeta
)
