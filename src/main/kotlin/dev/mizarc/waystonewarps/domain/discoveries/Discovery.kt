package dev.mizarc.waystonewarps.domain.discoveries

import java.util.*

/**
 * Maps warps to players. This is used to represent what players have access to what warps.
 * @property warpId The unique identifier for the warp.
 * @property playerId The unique identifier for a player.
 */
class Discovery(var warpId: UUID, var playerId: UUID)